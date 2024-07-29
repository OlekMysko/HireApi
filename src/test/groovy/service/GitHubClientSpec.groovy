package service

import atipera.com.hireapi.config.URLConfiguration
import atipera.com.hireapi.exception.GitHubApiException
import atipera.com.hireapi.exception.UserNotFoundException
import atipera.com.hireapi.model.RepositoryResponse
import atipera.com.hireapi.service.GitHubClient
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

class GitHubClientSpec extends Specification {
    def webClientBuilder = Mock(WebClient.Builder)
    def urlConfig = Mock(URLConfiguration)
    def webClient = Mock(WebClient)
    def client = new GitHubClient(webClientBuilder, urlConfig)
    def username = "testuser"

    def setup() {
        def requestHeadersSpec = Mock(WebClient.RequestHeadersUriSpec)
        def requestHeadersSpecWithUri = Mock(WebClient.RequestHeadersSpec)
        def responseSpec = Mock(WebClient.ResponseSpec)
        webClientBuilder.build() >> webClient
        webClient.get() >> requestHeadersSpec
        requestHeadersSpec.uri(_ as String) >> requestHeadersSpecWithUri
        requestHeadersSpecWithUri.retrieve() >> responseSpec
    }

    String buildUrl(String username) {
        urlConfig.getApi_url() >> "https://api.github.com"
        urlConfig.getUser_url_suffix() >> "/users/"
        urlConfig.getRepositories_url_suffix() >> "/repos"
        return "https://api.github.com/users/${username}/repos"
    }

    private mockWebClientGet(String url) {
        webClient.get() >> Mock(WebClient.RequestHeadersUriSpec)
        webClient.get().uri(url) >> Mock(WebClient.RequestHeadersSpec)
    }

    private mockWebClientRetrieve(String url) {
        mockWebClientGet(url)
        webClient.get().uri(url).retrieve() >> Mock(WebClient.ResponseSpec)
    }

    def "should handle successful response for repositories"() {
        given:
        def url = buildUrl(username)
        def repoResponse = new RepositoryResponse("repo1", new RepositoryResponse.Owner(username), false, [])
        mockWebClientRetrieve(url)
        webClient.get().uri(url).retrieve().bodyToMono(RepositoryResponse[].class) >> Mono.just([repoResponse] as RepositoryResponse[])

        when:
        def result = client.getRepositories(username).block()

        then:
        result == [repoResponse]
    }

    def "should handle 404 Not Found for repositories"() {
        given:
        def url = buildUrl(username)
        def notFoundException = WebClientResponseException.create(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null)
        mockWebClientRetrieve(url)
        webClient.get().uri(url).retrieve().bodyToMono(_) >> Mono.error(notFoundException)

        when:
        def result = client.getRepositories(username).block()

        then:
        thrown(UserNotFoundException)
    }

    def "should handle general WebClientException"() {
        given:
        def url = buildUrl(username)
        def generalException = WebClientResponseException.create(HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null)
        mockWebClientRetrieve(url)
        webClient.get().uri(url).retrieve().bodyToMono(_) >> Mono.error(generalException)

        when:
        def result = client.getRepositories(username).block()

        then:
        thrown(GitHubApiException)
    }
}
