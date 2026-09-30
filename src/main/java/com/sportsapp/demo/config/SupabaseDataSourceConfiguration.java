package com.sportsapp.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("!test")
public class SupabaseDataSourceConfiguration {
    @Bean
    public DataSource dataSource() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String configuredUrl = setting(dotenv, "SUPABASE_DB_URL");
        if (configuredUrl == null || configuredUrl.isBlank()) {
            throw new IllegalStateException("SUPABASE_DB_URL is required to connect to Supabase PostgreSQL");
        }

        URI databaseUri = URI.create(configuredUrl.startsWith("jdbc:")
                ? configuredUrl.substring("jdbc:".length())
                : configuredUrl);
        if (!"postgresql".equals(databaseUri.getScheme()) || databaseUri.getHost() == null) {
            throw new IllegalStateException("SUPABASE_DB_URL must be a postgresql:// connection URL");
        }

        String[] userInfo = databaseUri.getRawUserInfo() == null
                ? new String[0]
                : databaseUri.getRawUserInfo().split(":", 2);
        String username = setting(dotenv, "SUPABASE_DB_USER");
        if (username == null || username.isBlank()) {
            username = userInfo.length > 0 ? decode(userInfo[0]) : "postgres";
        }
        String password = setting(dotenv, "SUPABASE_DB_PASSWORD");
        if (password == null && userInfo.length > 1) {
            password = decode(userInfo[1]);
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("SUPABASE_DB_PASSWORD is required to connect to Supabase PostgreSQL");
        }

        String path = databaseUri.getPath() == null || databaseUri.getPath().isBlank()
                ? "/postgres"
                : databaseUri.getPath();
        String configuredHost = setting(dotenv, "SUPABASE_DB_HOST");
        String databaseHost = configuredHost == null || configuredHost.isBlank()
            ? databaseUri.getHost()
            : configuredHost;
        String configuredPort = setting(dotenv, "SUPABASE_DB_PORT");
        int databasePort = configuredPort == null || configuredPort.isBlank()
            ? (databaseUri.getPort() > 0 ? databaseUri.getPort() : 5432)
            : Integer.parseInt(configuredPort);
        String jdbcUrl = "jdbc:postgresql://" + databaseHost + ":"
            + databasePort
                + path + "?sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("euro-supabase");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(10000);
        return new HikariDataSource(config);
    }

    private static String setting(Dotenv dotenv, String name) {
        String environmentValue = System.getenv(name);
        return environmentValue == null || environmentValue.isBlank()
                ? dotenv.get(name)
                : environmentValue;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
