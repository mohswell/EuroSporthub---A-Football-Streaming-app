package com.sportsapp.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class LiveScoreApiClient {
    private static final String API_BASE_URL = "https://livescore-api.com/api-client";

    private final RestClient restClient;
    private final String apiKey;
    private final String apiSecret;

    public LiveScoreApiClient(
            RestClient.Builder restClientBuilder,
            @Qualifier("liveScoreApiKey") String apiKey,
            @Qualifier("liveScoreApiSecret") String apiSecret) {
        this.restClient = restClientBuilder.baseUrl(API_BASE_URL).build();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public JsonNode get(String endpoint, Map<String, String> parameters) {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "LiveScore API credentials are not configured");
        }

        JsonNode response = restClient.get()
                .uri(builder -> {
                    var uriBuilder = builder.path("/" + endpoint)
                            .queryParam("key", apiKey)
                            .queryParam("secret", apiSecret);
                    parameters.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "LiveScore API returned an empty response");
        }
        if (response.has("success") && !response.path("success").asBoolean()) {
            String message = response.path("error").asText("LiveScore API request failed");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
        }
        return response;
    }
}
