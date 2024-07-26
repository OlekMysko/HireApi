package atipera.com.hireapi.controller;

import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.RepositoryResponseDto;
import atipera.com.hireapi.service.GitHubRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(GitHubRepositoryController.BASE_API_URL)
public class GitHubRepositoryController {

    public static final String BASE_API_URL = "/api/github/repositories/";
    public static final String USER_NAME = "{username}";

    private final GitHubRepositoryService gitHubRepositoryService;

    @GetMapping(value = USER_NAME, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RepositoryResponseDto>> getRepositories(
            @PathVariable final String username, @RequestHeader(HttpHeaders.ACCEPT) final String acceptHeader)
            throws UserNotFoundException, GitHubApiException {
        List<RepositoryResponseDto> repositories = gitHubRepositoryService.getRepositories(username);
        return ResponseEntity.ok(repositories);
    }
}
