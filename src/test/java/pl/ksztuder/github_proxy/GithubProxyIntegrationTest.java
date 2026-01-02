package pl.ksztuder.github_proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubProxyIntegrationTest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(0));
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
        wireMockServer.resetAll();
    }

    @Test
    void shouldCompleteWithinTimeLimitWith3Requests() {

        int delay = 1000;

        stubFor(get(urlPathMatching("/users/.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withFixedDelay(delay) // Opóźnienie 1s
                .withBody("""
                    [
                      { "name": "repo-1", "owner": { "login": "user" }, "fork": false },
                      { "name": "repo-2", "owner": { "login": "user" }, "fork": false },
                      { "name": "repo-fork", "owner": { "login": "user" }, "fork": true }
                    ]
                """)));

        stubFor(get(urlPathMatching("/repos/.*/branches"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withFixedDelay(delay)
                .withBody("""
                    [ { "name": "main", "commit": { "sha": "123" } } ]
                """)));

        StopWatch stopWatch = StopWatch.createStarted();

        webTestClient.get()
            .uri("/api/github/test-user")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(RepositoryResponse.class)
            .hasSize(2); // Sprawdzamy czy fork został odfiltrowany (zostały 2 repo)

        stopWatch.stop();
        long totalTime = stopWatch.getTime(TimeUnit.MILLISECONDS);

        System.out.println(">>> Czas wykonania: " + totalTime + " ms");

        verify(3, getRequestedFor(urlMatching(".*")));
        assertThat(totalTime).isGreaterThan(2000);
        assertThat(totalTime).isLessThan(3200); // 3200ms to bezpieczny margines dla 3000ms
    }

    @Test
    void shouldReturn404() {
        stubFor(get(urlPathMatching("/users/.*"))
            .willReturn(aResponse().withStatus(404)));

        webTestClient.get()
            .uri("/api/github/unknown")
            .exchange()
            .expectStatus().isNotFound();
    }
}