package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorePriceDto {
    private String itemId;
    private String itemName;
    private String storeId;
    private BigDecimal priceAmount;
}
