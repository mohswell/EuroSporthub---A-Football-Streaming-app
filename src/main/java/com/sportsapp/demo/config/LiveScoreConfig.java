package com.sportsapp.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveScoreConfig {
    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public String liveScoreApiUrl() {
        return dotenv.get("LIVE_SCORE_API_URL");
    }

    @Bean
    public String liveScoreApiKey() {
        return dotenv.get("LIVE_SCORE_API_KEY");
    }

    @Bean
    public String liveScoreApiSecret() {
        return dotenv.get("LIVE_SCORE_API_SECRET");
    }
}
