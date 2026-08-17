package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.ReceiptItemMatchDto;
import com.ichat.cartculate.dto.ReceiptLineItemDto;
import com.ichat.cartculate.dto.ReceiptScanResultDto;
import com.ichat.cartculate.dto.ScanReceiptRequest;
import com.ichat.cartculate.entity.Item;
import com.ichat.cartculate.entity.Store;
import com.ichat.cartculate.repository.ItemRepository;
import com.ichat.cartculate.repository.StoreRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feature 7's AI Receipt Scanner backend: OCR + LLM matching of a
 * photographed/uploaded receipt against the master Item catalog.
 *
 * Calls the Anthropic Messages API directly over HTTPS using the JDK's
 * built-in java.net.http.HttpClient (no SDK dependency needed) plus the
 * JsonMapper this project already injects elsewhere (see
 * PurchaseHistoryService.java). Requires the ANTHROPIC_API_KEY environment
 * variable on the backend host - this throws immediately with a clear
 * message if it's missing, rather than failing silently mid-scan.
 *
 * The AI is given the ENTIRE current Item catalog (id/name/category) and is
 * instructed to reply only with matched itemIds it can see in that list, so
 * matchedItemName/category in the response always come from the real Item
 * entity - never trusted verbatim from the model's own text.
 */
@Service
public class ReceiptScanService {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_MODEL = "claude-sonnet-4-6";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;

    public ReceiptScanService(ItemRepository itemRepository, StoreRepository storeRepository, JsonMapper jsonMapper) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.jsonMapper = jsonMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    public ReceiptScanResultDto scanReceipt(ScanReceiptRequest request) {
        if (request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            throw new IllegalArgumentException("imageBase64 is required");
        }
        String mediaType = request.getMediaType() != null ? request.getMediaType() : "image/jpeg";

        List<Item> catalog = itemRepository.findAll();
        List<Store> stores = storeRepository.findAll();

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY environment variable is not set on the backend - "
                            + "the AI Receipt Scanner cannot run without it."
            );
        }

        JsonNode modelOutput = callAnthropic(apiKey, mediaType, request.getImageBase64(), catalog);
        return buildResultDto(modelOutput, catalog, stores);
    }

    /** Builds the vision + matching request and calls the Anthropic Messages API, returning the parsed JSON payload the model produced. */
    private JsonNode callAnthropic(String apiKey, String mediaType, String imageBase64, List<Item> catalog) {
        ArrayNode catalogArray = jsonMapper.createArrayNode();
        for (Item item : catalog) {
            ObjectNode entry = catalogArray.addObject();
            entry.put("id", item.getId());
            entry.put("name", item.getName());
            entry.put("category", item.getCategory());
        }
        String catalogJson = jsonMapper.writeValueAsString(catalogArray);

        String instructions = "You are reading a photographed grocery store receipt. "
                + "Extract every purchased line item: its raw printed text, quantity, and price per unit. "
                + "Then match each line to the closest entry in this master product catalog (by id): " + catalogJson + ". "
                + "Also guess the store name printed on the receipt, if visible. "
                + "Respond with ONLY valid JSON (no markdown fences, no commentary) in exactly this shape: "
                + "{\"storeName\": string|null, \"lineItems\": [{\"rawText\": string, \"quantity\": number, "
                + "\"pricePerUnit\": number, \"matchedItemId\": number|null, \"needsReview\": boolean, "
                + "\"alternativeItemIds\": number[]}]}. "
                + "Set needsReview=true whenever you are not highly confident in matchedItemId, and include up to 3 "
                + "plausible alternativeItemIds (from the catalog above) in that case. If nothing in the catalog is a "
                + "reasonable match at all, set matchedItemId to null and still list your best alternativeItemIds.";

        ObjectNode imageBlock = jsonMapper.createObjectNode();
        imageBlock.put("type", "image");
        ObjectNode imageSource = imageBlock.putObject("source");
        imageSource.put("type", "base64");
        imageSource.put("media_type", mediaType);
        imageSource.put("data", imageBase64);

        ObjectNode textBlock = jsonMapper.createObjectNode();
        textBlock.put("type", "text");
        textBlock.put("text", instructions);

        ArrayNode contentArray = jsonMapper.createArrayNode();
        contentArray.add(imageBlock);
        contentArray.add(textBlock);

        ObjectNode userMessage = jsonMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.set("content", contentArray);

        ObjectNode requestRoot = jsonMapper.createObjectNode();
        requestRoot.put("model", ANTHROPIC_MODEL);
        requestRoot.put("max_tokens", 4096);
        requestRoot.putArray("messages").add(userMessage);

        String requestBody = jsonMapper.writeValueAsString(requestRoot);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_API_URL))
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reach the Anthropic API for receipt scanning", e);
        }

        if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
            throw new RuntimeException("Anthropic API returned " + httpResponse.statusCode() + ": " + httpResponse.body());
        }

        JsonNode envelope = jsonMapper.readTree(httpResponse.body());
        // The model was instructed to reply with nothing but the JSON payload
        // as plain text, inside the Messages API's first content block.
        String modelText = envelope.path("content").path(0).path("text").asString("");
        return jsonMapper.readTree(stripMarkdownFences(modelText));
    }

    private ReceiptScanResultDto buildResultDto(JsonNode modelOutput, List<Item> catalog, List<Store> stores) {
        Map<Long, Item> itemsById = catalog.stream().collect(Collectors.toMap(Item::getId, i -> i));

        String guessedStoreName = modelOutput.path("storeName").isNull()
                ? null
                : modelOutput.path("storeName").asString(null);
        Store resolvedStore = resolveStore(guessedStoreName, stores);

        List<ReceiptLineItemDto> lineDtos = new ArrayList<>();
        int lineIndex = 0;
        for (JsonNode lineNode : modelOutput.path("lineItems")) {
            lineIndex++;

            JsonNode matchedIdNode = lineNode.path("matchedItemId");
            Long matchedId = (matchedIdNode.isNull() || matchedIdNode.isMissingNode()) ? null : matchedIdNode.asLong();
            Item matchedItem = matchedId != null ? itemsById.get(matchedId) : null;

            List<ReceiptItemMatchDto> alternatives = new ArrayList<>();
            for (JsonNode altIdNode : lineNode.path("alternativeItemIds")) {
                Item alt = itemsById.get(altIdNode.asLong());
                if (alt != null) {
                    alternatives.add(new ReceiptItemMatchDto(alt.getId().toString(), alt.getName()));
                }
            }
            // Always include the matched item itself as a selectable option in
            // the review dropdown, so switching away and back doesn't lose it.
            if (matchedItem != null
                    && alternatives.stream().noneMatch(a -> a.getItemId().equals(matchedItem.getId().toString()))) {
                alternatives.add(0, new ReceiptItemMatchDto(matchedItem.getId().toString(), matchedItem.getName()));
            }

            boolean needsReview = matchedItem == null || lineNode.path("needsReview").asBoolean(false);

            lineDtos.add(new ReceiptLineItemDto(
                    "line-" + lineIndex,
                    lineNode.path("rawText").asString(""),
                    matchedItem != null ? matchedItem.getId().toString() : null,
                    matchedItem != null ? matchedItem.getName() : "Unmatched item",
                    matchedItem != null ? matchedItem.getCategory() : "",
                    bigDecimalOrZero(lineNode.path("quantity")),
                    bigDecimalOrZero(lineNode.path("pricePerUnit")),
                    needsReview,
                    alternatives
            ));
        }

        return new ReceiptScanResultDto(
                "scan-" + System.currentTimeMillis(),
                resolvedStore != null ? resolvedStore.getId().toString() : null,
                resolvedStore != null ? resolvedStore.getName() : (guessedStoreName != null ? guessedStoreName : "Unknown store"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                lineDtos
        );
    }

    /**
     * Fuzzy, case-insensitive containment match against known stores (e.g. a
     * receipt printed "PUREGOLD MINDANAO AVE" still matches the "Puregold"
     * store row). Falls back to the first known store when nothing matches
     * or the model couldn't read a store name at all, so the frontend always
     * gets a real, usable storeId rather than having to handle "none".
     */
    private Store resolveStore(String guessedName, List<Store> stores) {
        if (guessedName != null) {
            String normalized = guessedName.trim().toLowerCase();
            for (Store store : stores) {
                String storeName = store.getName().toLowerCase();
                if (storeName.contains(normalized) || normalized.contains(storeName)) {
                    return store;
                }
            }
        }
        return stores.isEmpty() ? null : stores.get(0);
    }

    private BigDecimal bigDecimalOrZero(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(node.asString("0"));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }
}