package blogic.core.exception;

public class IllegalArgumentException extends AbstractCodedException {

    public IllegalArgumentException(String message) {
        super(400, message);
    }

}
