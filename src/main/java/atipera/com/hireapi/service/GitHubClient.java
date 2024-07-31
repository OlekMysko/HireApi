package atipera.com.hireapi.service;

import atipera.com.hireapi.config.URLConfiguration;
import atipera.com.hireapi.exception.GitHubApiException;
import atipera.com.hireapi.exception.UserNotFoundException;
import atipera.com.hireapi.model.BranchResponse;
import atipera.com.hireapi.model.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GitHubClient {

    private static final String ERROR_MSG = "Exception while communicating with GitHub API:";
    private final WebClient.Builder webClientBuilder;
    private final URLConfiguration urlConfig;

    public Mono<List<RepositoryResponse>> getRepositories(final String username) {
        String url = String.join(StringUtils.EMPTY, urlConfig.getApi_url(), urlConfig.getUser_url_suffix(), username, urlConfig.getRepositories_url_suffix());
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(RepositoryResponse[].class)
                .map(Arrays::asList)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.error(new UserNotFoundException("User not found: " + username)))
                .onErrorResume(WebClientException.class, ex -> Mono.error(new GitHubApiException(ERROR_MSG, ex)));
    }

    public Flux<BranchResponse> getBranches(final String username, final String repoName) {
        String url = String.join(StringUtils.EMPTY, urlConfig.getApi_url(), String.format(urlConfig.getBranches_url_suffix_template(), username, repoName));
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(BranchResponse.class)
                .onErrorResume(WebClientException.class, ex -> Flux.error(new GitHubApiException(ERROR_MSG, ex)));
    }
}