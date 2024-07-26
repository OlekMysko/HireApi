package atipera.com.hireapi.model;

public record BranchResponse(String name, ShaCommit shaCommit) {
    public record ShaCommit(String sha) {
    }
}