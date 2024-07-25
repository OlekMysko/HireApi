package atipera.com.hireapi.service;

import atipera.com.hireapi.config.URLConfiguration;
import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.BranchResponse;
import atipera.com.hireapi.model.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GitHubClient {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubClient.class);
    private final RestTemplate restTemplate;
    private final URLConfiguration urlConfig;

    public List<RepositoryResponse> getRepositories(final String username) throws UserNotFoundException {
        String url = String.join(StringUtils.EMPTY, urlConfig.getGithubApiUrl(), urlConfig.getUserUrlSuffix(), username, urlConfig.getRepositoriesUrlSuffix());
        return getListFromResponse(url, RepositoryResponse[].class, username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found: %s", username)));
    }

    public List<BranchResponse> getBranches(final String username, final String repoName) throws GitHubApiException, UserNotFoundException {
        String url = String.join(StringUtils.EMPTY, urlConfig.getGithubApiUrl(), String.format(urlConfig.getBranchesUrlSuffixTemplate(), username, repoName));
        try {
            return getListFromResponse(url, BranchResponse[].class, username)
                    .orElse(Collections.emptyList());
        } catch (UserNotFoundException e) {
            throw e;
        } catch (RestClientException e) {
            String msg = "Exception while communicating with GitHub API: {}";
            LOG.error(msg, url, e);
            throw new GitHubApiException(msg, e);
        }
    }

    private <T> Optional<List<T>> getListFromResponse(final String url, final Class<T[]> responseType, final String userName)
            throws UserNotFoundException {
        try {
            T[] response = restTemplate.getForObject(url, responseType);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(Arrays.asList(response));
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException(userName);
        } catch (RestClientException e) {
            String msg = "Exception while communicating with GitHub API";
            LOG.error(msg, e);
        }
        return Optional.empty();
    }
}

