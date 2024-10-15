package exception;

public class ImageWriteException extends Exception {

    public ImageWriteException() {
    }

    public ImageWriteException(Throwable cause) {
        super(cause);
    }

    public ImageWriteException(String message) {
        super(message);
    }
}
