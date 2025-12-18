package pl.ksztuder.github_proxy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import java.util.List;

@RestController
@RequestMapping("/api/github")
class GithubController {

    private final GithubService service;

    GithubController(GithubService service) {
        this.service = service;
    }

    @GetMapping("/{username}")
    List<RepositoryResponse> getRepos(@PathVariable String username) {
        return service.getUserRepositories(username);
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    ResponseEntity<ErrorResponse> handleNotFound(HttpClientErrorException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "User not found"));
    }
}