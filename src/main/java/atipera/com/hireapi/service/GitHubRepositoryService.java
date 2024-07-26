package atipera.com.hireapi.service;

import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.RepositoryResponse;
import atipera.com.hireapi.model.RepositoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubClient gitHubClient;

    public List<RepositoryResponseDto> getRepositories(final String username) throws UserNotFoundException, GitHubApiException {
        List<RepositoryResponse> repositories = gitHubClient.getRepositories(username);
        return getBranchesForRepositories(username, repositories);
    }

    private List<RepositoryResponseDto> getBranchesForRepositories(final String username, final List<RepositoryResponse> repositories) {
        return repositories.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> createRepositoryResponseDto(username, repo))
                .collect(Collectors.toList());
    }

    private RepositoryResponseDto createRepositoryResponseDto(String username, RepositoryResponse repo) {
        return new RepositoryResponseDto(
                repo.name(),
                repo.owner().login(),
                gitHubClient.getBranches(username, repo.name()));
    }
}
