package atipera.com.hireapi.service;

import atipera.com.hireapi.model.RepositoryResponse;
import atipera.com.hireapi.model.RepositoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {
    private final GitHubClient gitHubClient;

    public Mono<List<RepositoryResponseDto>> getRepositories(final String username) {
        return gitHubClient.getRepositories(username)
                .flatMapMany(Flux::fromIterable)
                .filter(repo -> !repo.fork())
                .flatMap(repo -> createRepositoryResponseDto(username, repo))
                .collectList();
    }

    private Mono<RepositoryResponseDto> createRepositoryResponseDto(String username, RepositoryResponse repo) {
        return gitHubClient.getBranches(username, repo.name())
                .collectList()
                .map(branches -> new RepositoryResponseDto(repo.name(), repo.owner().login(), branches));
    }
}