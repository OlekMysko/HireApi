package atipera.com.hireapi.model;

import java.util.List;


public record RepositoryResponse(String name, Owner owner, boolean fork,
                                 List<BranchResponse> branches) {
    public record Owner(String login) {
    }
}
