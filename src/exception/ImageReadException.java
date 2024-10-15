package exception;

public class ImageReadException extends Exception {

    public ImageReadException() {
    }

    public ImageReadException(String message) {
        super(message);
    }

    public ImageReadException(Throwable cause) {
        super(cause);
    }
}
