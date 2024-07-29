package controller

import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Unroll
import reactor.core.publisher.Mono
import org.springframework.http.HttpStatus
import atipera.com.hireapi.controller.GitHubRepositoryController
import atipera.com.hireapi.model.RepositoryResponseDto
import atipera.com.hireapi.service.GitHubRepositoryService

class GitHubRepositoryControllerSpec extends Specification {

    def gitHubRepositoryService = Mock(GitHubRepositoryService)
    def controller = new GitHubRepositoryController(gitHubRepositoryService)

    @Unroll
    def "should return repositories for username"() {
        given:
        def username = "testuser"
        def repoDto = new RepositoryResponseDto("repo1", username, [])
        gitHubRepositoryService.getRepositories(username) >> Mono.just([repoDto])

        when:
        def response = controller.getRepositories(username).block()

        then:
        response.statusCode == HttpStatus.OK
        response.body == [repoDto]
    }

    @Unroll
    def "should handle error when getting repositories"() {
        given:
        def username = "testuser"
        gitHubRepositoryService.getRepositories(username) >> Mono.error(new RuntimeException("Error"))

        when:
        def response = controller.getRepositories(username).onErrorResume { e ->
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
        }.block()

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    }
}
