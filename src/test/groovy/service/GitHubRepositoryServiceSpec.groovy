package service

import atipera.com.hireapi.exception.GitHubApiException
import atipera.com.hireapi.exception.UserNotFoundException
import atipera.com.hireapi.model.BranchResponse
import atipera.com.hireapi.model.RepositoryResponse
import atipera.com.hireapi.model.RepositoryResponseDto
import atipera.com.hireapi.service.GitHubClient
import atipera.com.hireapi.service.GitHubRepositoryService
import spock.lang.Specification
import spock.lang.Subject


class GitHubRepositoryServiceSpec extends Specification {
    GitHubClient gitHubClient = Mock()
    @Subject
    GitHubRepositoryService gitHubRepositoryService = new GitHubRepositoryService(gitHubClient)

    private final RepositoryResponse repoResponse = new RepositoryResponse( "repo1", new RepositoryResponse.Owner( "owner1"), false, [new BranchResponse("branch1",  new BranchResponse.Commit("sha1"))])
    private final List<RepositoryResponse> repositories = [repoResponse]
    private final List<BranchResponse> branches = [new BranchResponse( "branch1", new BranchResponse.Commit("sha1"))]

    def validUser() {
        given:
        String username = "validUser"
        gitHubClient.getRepositories(username) >> repositories
        gitHubClient.getBranches(username, "repo1") >> branches

        when:
        List<RepositoryResponseDto> result = gitHubRepositoryService.getRepositories(username)

        then:
        result.size() == 1
        result[0].repositoryName == "repo1"
        result[0].ownerLogin == "owner1"
        result[0].branches.size() == 1
        result[0].branches[0].name == "branch1"
        result[0].branches[0].Commit.sha == "sha1"
    }

    def nonExistentUser() {
        given:
        String username = "nonExistentUser"
        gitHubClient.getRepositories(username) >> { throw new UserNotFoundException("User not found") }

        when:
        gitHubRepositoryService.getRepositories(username)

        then:
        thrown(UserNotFoundException)
    }

    def apiError() {
        given:
        String username = "userWithApiError"
        gitHubClient.getRepositories(username) >> { throw new GitHubApiException("GitHub API error", new Throwable()) }

        when:
        gitHubRepositoryService.getRepositories(username)

        then:
        thrown(GitHubApiException)
    }
}
