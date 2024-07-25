package atipera.com.hireapi.exception;

public class GitHubApiException extends Exception {
    public GitHubApiException(final String message, final Throwable cause) {
        super(message, cause);
    }
}