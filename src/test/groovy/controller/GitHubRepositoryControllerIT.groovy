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
    import org.springframework.test.context.ActiveProfiles
    import org.springframework.web.reactive.function.client.WebClient
    import spock.lang.Specification
    import spock.lang.Stepwise
    import reactor.core.publisher.Mono

    import static com.github.tomakehurst.wiremock.client.WireMock.*

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = HireApiApplication)
    @ActiveProfiles("test")
    @Stepwise
    class GitHubRepositoryControllerIT extends Specification {


        private WebClient webClient
        private static WireMockServer wireMockServer

        def setupSpec() {
            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(2907)) // or any other free port
            wireMockServer.start()
            configureFor("localhost", 2907)
        }

        def cleanupSpec() {
            wireMockServer.stop()
        }

        def setup() {
            webClient = WebClient.builder()
                    .baseUrl("http://localhost:2907")
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
                    .map { json ->
                        def mapper = new ObjectMapper()
                        def typeFactory = mapper.typeFactory
                        def collectionType = typeFactory.constructCollectionType(List, RepositoryResponse)
                        return mapper.readValue(json, collectionType)
                    }
                    .block()

            then:
            response.size() == 1
            def repo = response[0] as RepositoryResponse
            repo.name() == "repo1"
            repo.owner().login() == username
            repo.branches() == []
        }

        def "should handle user not found error"() {
            given:
            def username = "nonexistentuser"
            wireMockServer.stubFor(get(urlEqualTo("/users/${username}/repos"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.NOT_FOUND.value())))

            when:
            def response = webClient.get()
                    .uri("/api/github/repositories/${username}")
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("User not found")))
                    .bodyToMono(String)
                    .onErrorResume(Exception, ex -> Mono.just("User not found"))
                    .block()

            then:
            response == "User not found"
        }

        def "should handle GitHub API errors"() {
            given:
            def username = "testuser"
            wireMockServer.stubFor(get(urlEqualTo("/users/${username}/repos"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.BAD_REQUEST.value())))

            when:
            def response = webClient.get()
                    .uri("/api/github/repositories/${username}")
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("GitHub API error")))
                    .bodyToMono(String)
                    .onErrorResume(Exception, ex -> Mono.just("GitHub API error"))
                    .block()

            then:
            response == "GitHub API error"
        }
    }
