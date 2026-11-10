package com.finsight.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Global Cross-Origin Resource Sharing (CORS) Configuration for Spring Cloud Gateway.
 *
 * Configured as a reactive {@link CorsWebFilter} running on the Netty pipeline,
 * intercepting preflight OPTIONS requests before downstream dispatch.
 * Explicitly exposes critical trading headers such as {@code Idempotency-Key}.
 */
@Configuration
public class CorsConfig {

    @Value("${finsight.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed client origins (e.g. React Vite on 5173, Next.js on 3000)
        config.setAllowedOrigins(allowedOrigins);

        // Preflight response caching duration (1 hour to minimize OPTIONS chatter)
        config.setMaxAge(3600L);

        // Permitted HTTP verbs
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Permitted request headers
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "Idempotency-Key",
                "X-Requested-With",
                "X-User-Id",
                "X-User-Roles"
        ));

        // Headers exposed to browser JS (Axios response headers)
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Idempotency-Key",
                "X-Total-Count",
                "X-Execution-Time"
        ));

        // Allow sending session cookies and authorization credentials
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

