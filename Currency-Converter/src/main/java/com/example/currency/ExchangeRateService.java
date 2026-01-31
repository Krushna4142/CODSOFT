package com.example.currency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.time.Duration;

public class ExchangeRateService {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    private static final Map<String, BigDecimal> USD_BASED = Map.of(
            "USD", new BigDecimal("1.0"),
            "EUR", new BigDecimal("0.92"),
            "GBP", new BigDecimal("0.78"),
            "INR", new BigDecimal("83.10"),
            "JPY", new BigDecimal("148.0"),
            "AUD", new BigDecimal("1.50"),
            "CAD", new BigDecimal("1.35"),
            "CHF", new BigDecimal("0.85"),
            "CNY", new BigDecimal("7.20"),
            "NZD", new BigDecimal("1.64")
    );

    // Immediate local rate using fixed table; always returns quickly.
    public BigDecimal quickLocalRate(String from, String to) {
        if (from != null && from.equalsIgnoreCase(to)) return BigDecimal.ONE;
        return fallbackCrossRate(from, to);
    }

    public BigDecimal fetchRate(String from, String to) {
        // If both currencies are the same, the rate is 1
        if (from != null && from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }

        try {
            // Use exchangerate.host convert endpoint with amount=1 to get unit rate
            String url = String.format("https://api.exchangerate.host/convert?from=%s&to=%s&amount=1", from, to);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return fallbackCrossRate(from, to);
            }
            JsonNode root = mapper.readTree(response.body());
            // Expected schema: { "success": true, "result": <number> }
            JsonNode successNode = root.path("success");
            if (successNode.isBoolean() && !successNode.booleanValue()) {
                return fallbackCrossRate(from, to);
            }
            JsonNode resultNode = root.path("result");
            if (resultNode.isNumber()) {
                return resultNode.decimalValue();
            }
            return fallbackCrossRate(from, to);
        } catch (IOException | InterruptedException e) {
            return fallbackCrossRate(from, to);
        }
    }

    private BigDecimal fallbackCrossRate(String from, String to) {
        if (from == null || to == null) return null;
        BigDecimal usdToFrom = USD_BASED.get(from.toUpperCase());
        BigDecimal usdToTo = USD_BASED.get(to.toUpperCase());
        if (usdToFrom == null || usdToTo == null) return null;
        return usdToTo.divide(usdToFrom, MC);
    }
}
