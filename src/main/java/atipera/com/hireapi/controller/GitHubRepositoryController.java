package atipera.com.hireapi.controller;

import atipera.com.hireapi.model.RepositoryResponseDto;
import atipera.com.hireapi.service.GitHubRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(GitHubRepositoryController.BASE_API_URL)
public class GitHubRepositoryController {

    public static final String BASE_API_URL = "/api/github/repositories/";
    public static final String USER_NAME = "{username}";

    private final GitHubRepositoryService gitHubRepositoryService;

    @GetMapping(value = USER_NAME, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<RepositoryResponseDto>>> getRepositories(@PathVariable final String username) {
        return gitHubRepositoryService.getRepositories(username).map(ResponseEntity::ok);
    }
}
