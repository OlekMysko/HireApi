package atipera.com.hireapi.model;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryResponse {
    private String name;
    private Owner owner;

    private boolean fork;
    private List<BranchResponse> branches;

    @Data
    public static class Owner {
        private String login;
    }
}
