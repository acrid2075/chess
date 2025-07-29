package ui;

public class ResponseException extends RuntimeException {
    int status;
    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }
}
