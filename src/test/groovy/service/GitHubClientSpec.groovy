package service

import atipera.com.hireapi.config.URLConfiguration
import atipera.com.hireapi.exception.UserNotFoundException
import atipera.com.hireapi.model.BranchResponse
import atipera.com.hireapi.model.RepositoryResponse
import atipera.com.hireapi.service.GitHubClient
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class GitHubClientSpec extends Specification {

    RestTemplate restTemplate = Mock()
    URLConfiguration urlConfiguration = Mock()

    @Subject
    GitHubClient gitHubClient = new GitHubClient(restTemplate, urlConfiguration)

    @Unroll
    def "should get repositories for username #username"() {
        given:
        urlConfiguration.api_url >> "https://api.github.com"
        urlConfiguration.user_url_suffix >> "/users/"
        urlConfiguration.repositories_url_suffix >> "/repos"
        String url = "https://api.github.com/users/$username/repos"
        RepositoryResponse[] responseArray = [
                new RepositoryResponse("repo1", new RepositoryResponse.Owner("owner1"), false, List.of(
                        new BranchResponse("master", new BranchResponse.Commit("5072be5b55fcaeabb1397a24bed4e792a1c5ead9"))
                ))
        ]

        restTemplate.getForObject(url, RepositoryResponse[].class) >> responseArray

        when:
        List<RepositoryResponse> result = gitHubClient.getRepositories(username)

        then:
        result.size() == 1
        result[0].name == "repo1"
        result[0].owner().login() == "owner1"

        where:
        username << ["validUser", "anotherUser"]
    }

    def "should throw UserNotFoundException when user is not found"() {
        given:
        urlConfiguration.api_url >> "https://api.github.com"
        urlConfiguration.user_url_suffix >> "/users/"
        urlConfiguration.repositories_url_suffix >> "/repos"
        String url = "https://api.github.com/users/nonExistentUser/repos"

        restTemplate.getForObject(url, RepositoryResponse[].class) >> { throw new HttpClientErrorException(HttpStatus.NOT_FOUND) }

        when:
        gitHubClient.getRepositories("nonExistentUser")

        then:
        thrown(UserNotFoundException)
    }
}
