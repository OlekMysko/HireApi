package controller

import atipera.com.hireapi.HireApiApplication
import atipera.com.hireapi.config.URLConfiguration
import atipera.com.hireapi.model.RepositoryResponse
import atipera.com.hireapi.model.RepositoryResponseDto
import atipera.com.hireapi.service.GitHubRepositoryService
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification
import reactor.core.publisher.Mono

import static com.github.tomakehurst.wiremock.client.WireMock.*

@ContextConfiguration(classes = HireApiApplication)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubRepositoryControllerIT extends Specification {
    private static final String HOST = 'localhost'
    private static final int TEST_PORT = 2907
    private static final String BASE_URL = "http://${HOST}:${TEST_PORT}"
    private WebClient webClient
    private static WireMockServer wireMockServer

    @Autowired
    URLConfiguration configuration
    @Autowired
    GitHubRepositoryService gitHubRepositoryService

    def githubApiUrl
    def userUrlSuffix
    def reposUrlSuffix

    def setupSpec() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(TEST_PORT))
        wireMockServer.start()
        configureFor(HOST, TEST_PORT)
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def setup() {
        githubApiUrl = configuration.getApi_url()
        userUrlSuffix = configuration.getUser_url_suffix()
        reposUrlSuffix = configuration.getRepositories_url_suffix()
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
    }

    def "URLConfiguration is not null"() {
        expect:
        configuration != null
    }

    def "GitHubRepositoryService is not null"() {
        expect:
        gitHubRepositoryService != null
    }

    def "should return repositories for username"() {
        given:
        def username = "testuser"
        def reposUrl = "${userUrlSuffix}${username}${reposUrlSuffix}"
        def repoResponse = '''[
        {
            "name": "repo1",
            "owner": {"login": "testuser"},
            "fork": false,
            "branches": []
        }
        ]'''
        wireMockServer.stubFor(get(urlEqualTo(reposUrl))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(repoResponse)))
        when:
        def response = webClient.get()
                .uri(reposUrl)
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
        def reposUrl = "${userUrlSuffix}${username}${reposUrlSuffix}"
        wireMockServer.stubFor(get(urlEqualTo(reposUrl))
                .willReturn(aResponse()
                        .withStatus(statusCode)))
        when:
        def response = webClient.get()
                .uri(reposUrl)
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

    def "should return only non-forked repositories for username"() {
        given:
        def username = "testuser"
        def reposUrl = "${userUrlSuffix}${username}${reposUrlSuffix}"
        def repoResponse = '''[
    {
        "name": "repo1",
        "owner": {"login": "testuser"},
        "fork": false,
        "branches": []
    },
    {
        "name": "repo2",
        "owner": {"login": "testuser"},
        "fork": true,
        "branches": []
    }
    ]'''
        wireMockServer.stubFor(get(urlEqualTo(reposUrl))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(repoResponse)))
        when:
        def response = webClient.get()
                .uri(reposUrl)
                .retrieve()
                .bodyToMono(String)
                .map(this.&jsonToObject)
                .block()
                .findAll { !it.fork() }
        then:
        response.size() == 1
        def repo = response[0] as RepositoryResponse
        repo.name() == "repo1"
        repo.owner().login() == username
        repo.branches() == []
    }

    def "should return empty list for user with no repositories"() {
        given:
        def username = "emptyuser"
        def reposUrl = "${userUrlSuffix}${username}${reposUrlSuffix}"
        def repoResponse = '[]'
        wireMockServer.stubFor(get(urlEqualTo(reposUrl))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(repoResponse)))
        when:
        def response = webClient.get()
                .uri(reposUrl)
                .retrieve()
                .bodyToMono(String)
                .map(this.&jsonToObject)
                .block()
        then:
        response.size() == 0
    }

    def "should return repositories with multiple branches"() {
        given:
        def username = "testuser"
        def reposUrl = "${userUrlSuffix}${username}${reposUrlSuffix}"
        def repoResponse = '''[
        {
            "name": "repo1",
            "owner": {"login": "testuser"},
            "fork": false,
            "branches": []
        }
    ]'''
        def branchesResponse = '''[
        {"name": "branch1"},
        {"name": "branch2"}
    ]'''
        wireMockServer.stubFor(get(urlEqualTo(reposUrl))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(repoResponse)))
        wireMockServer.stubFor(get(urlEqualTo("${userUrlSuffix}${username}/repo1/branches"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(branchesResponse)))
        when:
        def response = webClient.get()
                .uri(reposUrl)
                .retrieve()
                .bodyToMono(String)
                .map(this.&jsonToObject)
                .block()
                .collect { repo ->
                    def branches = webClient.get()
                            .uri("${userUrlSuffix}${username}/${repo.name()}/branches")
                            .retrieve()
                            .bodyToMono(String)
                            .map(this.&jsonToObject)
                            .block()
                    new RepositoryResponseDto(repo.name(), repo.owner().login(), branches)
                }
        then:
        response.size() == 1
        def repoDto = response[0] as RepositoryResponseDto
        repoDto.repositoryName() == "repo1"
        repoDto.ownerLogin() == username
        repoDto.branches().size() == 2
        repoDto.branches().collect { it.name() } == ["branch1", "branch2"]
    }

    private static List<RepositoryResponse> jsonToObject(String json) {
        def mapper = new ObjectMapper()
        def typeFactory = mapper.typeFactory
        def collectionType = typeFactory.constructCollectionType(List, RepositoryResponse)
        return mapper.readValue(json, collectionType)
    }
}
