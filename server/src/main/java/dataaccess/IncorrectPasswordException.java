package dataaccess;

public class IncorrectPasswordException extends Exception{
    public IncorrectPasswordException(String message) {
        super(message);
    }
    public IncorrectPasswordException(String message, Throwable ex) {
        super(message, ex);
    }
}
