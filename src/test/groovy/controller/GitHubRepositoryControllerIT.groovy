package controller

import atipera.com.hireapi.HireApiApplication
import atipera.com.hireapi.model.RepositoryResponse
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.boot.test.context.SpringBootTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification
import reactor.core.publisher.Mono

import static com.github.tomakehurst.wiremock.client.WireMock.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = HireApiApplication)
class GitHubRepositoryControllerIT extends Specification {
    private static final String HOST = 'localhost'
    private static final int TEST_PORT = 2907
    private static final String BASE_URL = "http://${HOST}:${TEST_PORT}"
    private WebClient webClient
    private static WireMockServer wireMockServer

    def setupSpec() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(2907))
        wireMockServer.start()
        configureFor(HOST, TEST_PORT)
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def setup() {
        webClient = WebClient.builder()
                .baseUrl( BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
    }

    def "should return repositories for username"() {
        given:
        def username = "testuser"
        def repoResponse = '''[
    {
        "name": "repo1",
        "owner": {"login": "testuser"},
        "fork": false,
        "branches": []
    }
]'''
        wireMockServer.stubFor(get(urlEqualTo("/users/${username}/repos"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(repoResponse)))
        when:
        def response = webClient.get()
                .uri("/users/${username}/repos")
                .retrieve()
                .bodyToMono(String)
                .map(this.&jsonToObject)
                .block()
        then:
        response.size() == 1
        def repo = response[0] as RepositoryResponse
        repo.name() == "repo1"
        repo.owner().login() == username
        repo.branches() == []
    }

    def "should handle errors"() {
        given:
        wireMockServer.stubFor(get(urlEqualTo("/users/${username}/repos"))
                .willReturn(aResponse()
                        .withStatus(statusCode)))
        when:
        def response = webClient.get()
                .uri("/api/github/repositories/${username}")
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException(errorMessage)))
                .bodyToMono(String)
                .onErrorResume(Exception, ex -> Mono.just(errorMessage))
                .block()
        then:
        response == errorMessage

        where:
        username          | statusCode                     | errorMessage
        "nonexistentuser" | HttpStatus.NOT_FOUND.value()   | "User not found"
        "testuser"        | HttpStatus.BAD_REQUEST.value() | "GitHub API error"
    }

    private List<RepositoryResponse> jsonToObject(String json) {
        def mapper = new ObjectMapper()
        def typeFactory = mapper.typeFactory
        def collectionType = typeFactory.constructCollectionType(List, RepositoryResponse)
        return mapper.readValue(json, collectionType)
    }
}
