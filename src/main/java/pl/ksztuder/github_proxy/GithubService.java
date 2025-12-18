package pl.ksztuder.github_proxy;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getUserRepositories(String username) {
        List<GithubRepo> repos = githubClient.getUserRepos(username);

        return repos.stream()
            .filter(repo -> !repo.fork())
            .map(repo -> {
                var branches = githubClient.getBranches(repo.owner().login(), repo.name());

                var branchResponses = branches.stream()
                    .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                    .toList();

                return new RepositoryResponse(repo.name(), repo.owner().login(), branchResponses);
            })
            .toList();
    }
}