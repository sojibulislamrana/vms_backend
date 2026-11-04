package com.sojibulislamrana.vms_backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Intelligent DataSource configuration that automatically parses and normalizes
 * standard cloud database URLs (such as Render's postgresql://user:pass@host/db)
 * into JDBC-compliant URLs (jdbc:postgresql://host:port/db) while safely supporting
 * local SQLite and traditional JDBC URLs.
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:${DB_URL:${DATABASE_URL:jdbc:sqlite:vms.db}}}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = (rawUrl != null && !rawUrl.isBlank()) ? rawUrl.trim() : "jdbc:sqlite:vms.db";
        String user = (username != null) ? username.trim() : "";
        String pass = (password != null) ? password.trim() : "";
        String driver = (driverClassName != null) ? driverClassName.trim() : "";

        // Auto-convert cloud URLs formatted as postgresql:// or postgres://
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            try {
                String uriString = url.startsWith("postgresql://")
                        ? "http" + url.substring("postgresql".length())
                        : "http" + url.substring("postgres".length());

                URI uri = new URI(uriString);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    user = parts[0];
                    pass = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String query = uri.getQuery();
                if (query != null && !query.isEmpty()) {
                    url = String.format("jdbc:postgresql://%s:%d/%s?%s", host, port, path, query);
                } else {
                    url = String.format("jdbc:postgresql://%s:%d/%s", host, port, path);
                }
                driver = "org.postgresql.Driver";
            } catch (Exception ignored) {
                if (!url.startsWith("jdbc:")) {
                    url = "jdbc:" + url;
                }
            }
        }

        if (url.startsWith("jdbc:postgresql:") && driver.isEmpty()) {
            driver = "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:sqlite:") && driver.isEmpty()) {
            driver = "org.sqlite.JDBC";
        }

        config.setJdbcUrl(url);

        if (!user.isEmpty()) {
            config.setUsername(user);
        }
        if (!pass.isEmpty()) {
            config.setPassword(pass);
        }
        if (!driver.isEmpty()) {
            config.setDriverClassName(driver);
        }

        return new HikariDataSource(config);
    }
}
