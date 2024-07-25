package atipera.com.hireapi.exception;


public class UserNotFoundException extends Exception {
    public UserNotFoundException(final String message) {
        super(message);
    }
}