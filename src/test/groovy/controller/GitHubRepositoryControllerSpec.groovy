package controller

import atipera.com.hireapi.controller.GitHubRepositoryController
import atipera.com.hireapi.exception.GitHubApiException
import atipera.com.hireapi.exception.GlobalExceptionHandler
import atipera.com.hireapi.exception.UserNotFoundException
import atipera.com.hireapi.model.RepositoryResponseDto
import atipera.com.hireapi.service.GitHubRepositoryService
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content

class GitHubRepositoryControllerSpec extends Specification {

    MockMvc mockMvc
    GitHubRepositoryService gitHubRepositoryService = Mock()

    @Subject
    GitHubRepositoryController gitHubRepositoryController = new GitHubRepositoryController(gitHubRepositoryService)

    def setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(gitHubRepositoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "should return repositories for valid username and Accept header"() {
        given:
        String username = "validUser"
        String acceptHeader = "application/json"
        List<RepositoryResponseDto> repositories = [new RepositoryResponseDto("repo1", "owner1", [])]

        gitHubRepositoryService.getRepositories(username) >> repositories

        when:
        def result = mockMvc.perform(get("/api/github/repositories/${username}")
                .header(HttpHeaders.ACCEPT, acceptHeader))

        then:
        result.andExpect(status().isOk())
                .andExpect(content().contentType(acceptHeader))
                .andExpect(content().json('[{"repositoryName":"repo1","ownerLogin":"owner1","branches":[]}]'))
    }

    def "should return 406 Not Acceptable for invalid Accept header"() {
        given:
        String username = "validUser"
        String acceptHeader = "text/plain"

        when:
        def result = mockMvc.perform(get("/api/github/repositories/${username}")
                .header(HttpHeaders.ACCEPT, acceptHeader))

        then:
        result.andExpect(status().isNotAcceptable())
    }

    def "should return 404 Not Found when user does not exist"() {
        given:
        String username = "nonExistentUser"
        String acceptHeader = "application/json"

        gitHubRepositoryService.getRepositories(username) >> { throw new UserNotFoundException("User not found") }

        when:
        def result = mockMvc.perform(get("/api/github/repositories/${username}")
                .header(HttpHeaders.ACCEPT, acceptHeader))

        then:
        result.andExpect(status().isNotFound())
                .andExpect(content().json('{"status":404,"message":"User not found"}'))
    }

    def "should return 400 Bad Request on GitHub API error"() {
        given:
        String username = "userWithApiError"
        String acceptHeader = "application/json"

        gitHubRepositoryService.getRepositories(username) >> { throw new GitHubApiException("GitHub API error", new Throwable()) }

        when:
        def result = mockMvc.perform(get("/api/github/repositories/${username}")
                .header(HttpHeaders.ACCEPT, acceptHeader))

        then:
        result.andExpect(status().isBadRequest())
                .andExpect(content().json('{"status":400,"message":"GitHub API error"}'))
    }
}
