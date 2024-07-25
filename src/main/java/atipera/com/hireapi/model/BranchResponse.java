package atipera.com.hireapi.model;

import lombok.Data;

@Data
public class BranchResponse {
    private String name;
    private Commit commit;

    @Data
    public static class Commit {
        private String sha;
    }
}