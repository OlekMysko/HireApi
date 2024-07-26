package atipera.com.hireapi.model;

public record BranchResponse(String name, Commit commit) {
    public record Commit(String sha) {
    }
}
