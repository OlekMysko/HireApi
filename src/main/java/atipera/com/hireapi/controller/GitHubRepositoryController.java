package atipera.com.hireapi.controller;

import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.RepositoryResponseDto;
import atipera.com.hireapi.service.GitHubRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static atipera.com.hireapi.config.URL.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_API_URL)
public class GitHubRepositoryController {

    private final GitHubRepositoryService gitHubRepositoryService;

    @GetMapping(USER_NAME)
    public ResponseEntity<List<RepositoryResponseDto>> getRepositories(
            @PathVariable final String username, @RequestHeader(HttpHeaders.ACCEPT) final String acceptHeader)
            throws UserNotFoundException, GitHubApiException {

        if (!JSON_ACCEPT_HEADER.equals(acceptHeader)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        List<RepositoryResponseDto> repositories = gitHubRepositoryService.getRepositories(username);
        return ResponseEntity.ok(repositories);
    }
}
