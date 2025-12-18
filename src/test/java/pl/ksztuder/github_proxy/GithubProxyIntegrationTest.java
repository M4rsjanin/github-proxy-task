package pl.ksztuder.github_proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

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
    }

    @Test
    void shouldReturnRepos() {
        // GIVEN
        stubFor(get(urlPathMatching("/users/.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                      {
                        "name": "test-repo",
                        "owner": { "login": "user" },
                        "fork": false
                      }
                    ]
                """)));

        stubFor(get(urlPathMatching("/repos/.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                      {
                        "name": "main",
                        "commit": { "sha": "123" }
                      }
                    ]
                """)));

        // WHEN & THEN
        webTestClient.get()
            .uri("/api/github/test-user")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(RepositoryResponse.class)
            .hasSize(1)
            .value(repos ->
                assertThat(repos.getFirst().branches().getFirst().name()).isEqualTo("main")
            );
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