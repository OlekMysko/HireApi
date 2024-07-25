package atipera.com.hireapi.service;

import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.RepositoryResponse;
import atipera.com.hireapi.model.RepositoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubRepositoryService.class);
    private final GitHubClient gitHubClient;

    public List<RepositoryResponseDto> getRepositories(final String username) throws UserNotFoundException, GitHubApiException {
        try {
            List<RepositoryResponse> repositories = gitHubClient.getRepositories(username);
            return getBranchesForRepositories(username, repositories);
        } catch (UserNotFoundException e) {
            LOG.warn("User not found: {}", username);
            throw e;
        } catch (HttpClientErrorException e) {
            LOG.error("Error while communicating with GitHub API", e);
            throw new GitHubApiException("Error while communicating with GitHub API", e);
        }
    }

    private List<RepositoryResponseDto> getBranchesForRepositories(final String username, final List<RepositoryResponse> repositories) {
        return repositories.stream()
                .filter(repo -> !repo.isFork())
                .map(repo -> {
                    try {
                        return new RepositoryResponseDto(
                                repo.getName(),
                                repo.getOwner().getLogin(),
                                gitHubClient.getBranches(username, repo.getName()));
                    } catch (GitHubApiException e) {
                        LOG.error("An error occurred while getting the branches for the repository {}", repo.getName(), e);
                        throw new RuntimeException(e);
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
