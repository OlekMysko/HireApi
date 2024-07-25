package atipera.com.hireapi.model;

import java.util.List;

public record RepositoryResponseDto(String repositoryName, String ownerLogin, List<BranchResponse> branches) {
}