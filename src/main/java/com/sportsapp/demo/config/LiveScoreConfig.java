package com.sportsapp.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveScoreConfig {
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    @Bean
    public String liveScoreApiUrl() {
        return value("LIVE_SCORE_API_URL");
    }

    @Bean
    public String liveScoreApiKey() {
        return value("LIVE_SCORE_API_KEY");
    }

    @Bean
    public String liveScoreApiSecret() {
        return value("LIVE_SCORE_API_SECRET");
    }

    @Bean
    public String euroSyncToken() {
        return value("EURO_SYNC_TOKEN");
    }

    private String value(String name) {
        String environmentValue = System.getenv(name);
        if (environmentValue != null) {
            return environmentValue;
        }
        String dotenvValue = dotenv.get(name);
        return dotenvValue == null ? "" : dotenvValue;
    }
}
