package com.sportsapp.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String origin = System.getenv("WEB_ORIGIN");
        if (origin == null || origin.isBlank()) {
            origin = dotenv.get("WEB_ORIGIN", "http://localhost:3000");
        }
        String[] origins = Arrays.stream(origin.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "X-Sync-Token")
                        .maxAge(3600);
            }
        };
    }
}