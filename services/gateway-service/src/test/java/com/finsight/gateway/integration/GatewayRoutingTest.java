package com.finsight.gateway.integration;

import com.finsight.gateway.GatewayApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Should expose Actuator health check with UP status")
    void shouldReturnActuatorHealth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("Should successfully register all 4 downstream microservice routes")
    void shouldRegisterAllRoutes() {
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        var routeIds = routes.stream().map(r -> r.getId()).toList();
        assertThat(routeIds).contains(
                "portfolio-service-route",
                "market-data-service-route",
                "alert-service-route",
                "research-service-route"
        );
    }

    @Test
    @DisplayName("Should handle CORS preflight OPTIONS request from React dev server")
    void shouldHandleCorsPreflight() {
        webTestClient.options()
                .uri("http://localhost:8080/api/v1/portfolios/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type,Idempotency-Key")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
    }

    @Test
    @DisplayName("Should reject CORS preflight from untrusted origin")
    void shouldRejectCorsFromUntrustedOrigin() {
        webTestClient.options()
                .uri("http://localhost:8080/api/v1/portfolios/1")
                .header(HttpHeaders.ORIGIN, "http://malicious-website.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .exchange()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }
}
