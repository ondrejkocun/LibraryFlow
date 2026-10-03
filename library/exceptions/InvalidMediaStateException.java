package library.exceptions;

public class InvalidMediaStateException extends RuntimeException {
    public InvalidMediaStateException(String message) {
        super(message);
    }
}
