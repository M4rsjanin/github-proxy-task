package pl.ksztuder.github_proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


record GithubRepo(String name, Owner owner, boolean fork) {}
record Owner(String login) {}
record GithubBranch(String name, Commit commit) {}
record Commit(String sha) {}

record RepositoryResponse(
    @JsonProperty("Repository Name") String repositoryName,
    @JsonProperty("Owner Login") String ownerLogin,
    List<BranchResponse> branches
) {}

record BranchResponse(String name, String lastCommitSha) {}

record ErrorResponse(int status, String message) {}
