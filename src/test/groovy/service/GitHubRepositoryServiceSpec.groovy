package service

import atipera.com.hireapi.model.BranchResponse
import atipera.com.hireapi.model.RepositoryResponse
import atipera.com.hireapi.model.RepositoryResponseDto
import atipera.com.hireapi.service.GitHubClient
import atipera.com.hireapi.service.GitHubRepositoryService
import spock.lang.Specification
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class GitHubRepositoryServiceSpec extends Specification {

    def gitHubClient = Mock(GitHubClient)
    def service = new GitHubRepositoryService(gitHubClient)

    def "should return RepositoryResponseDto list for repositories"() {
        given:
        def username = "testuser"
        def repo = new RepositoryResponse(
                "repo1",
                new RepositoryResponse.Owner("testuser"),
                false,
                []
        )
        def branches = [
                new BranchResponse("branch1", new BranchResponse.Commit("sha1"))
        ]

        gitHubClient.getRepositories(username) >> Mono.just([repo])
        gitHubClient.getBranches(username, repo.name()) >> Flux.fromIterable(branches)

        when:
        def result = service.getRepositories(username).block()

        then:
        result == [new RepositoryResponseDto(repo.name(), repo.owner().login(), branches)]
    }
}
