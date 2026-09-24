package com.ichat.cartculate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * GET /health - plain "is the backend up" check, for use from a phone's
 * browser or an uptime monitor. Returns 200 with a small JSON body as long
 * as the app itself is running; it does NOT check the database, so a 200
 * here doesn't guarantee /api calls will succeed too (Render's own health
 * check hits this same path to decide whether to route traffic to this
 * instance).
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString()
        ));
    }
}
