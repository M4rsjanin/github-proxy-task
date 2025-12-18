package pl.ksztuder.github_proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
class GithubClient {

    private final RestClient restClient;

    GithubClient(@Value("${github.api.url}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Accept", "application/json")
            .build();
    }

    List<GithubRepo> getUserRepos(String username) {
        return restClient.get()
            .uri("/users/{username}/repos", username)
            .retrieve()
            .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }

    List<GithubBranch> getBranches(String username, String repoName) {
        return restClient.get()
            .uri("/repos/{username}/{repo}/branches", username, repoName)
            .retrieve()
            .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}