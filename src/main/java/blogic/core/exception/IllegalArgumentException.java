package blogic.core.exception;

public class IllegalArgumentException extends AbstractCodedException {

    public IllegalArgumentException(Object message) {
        super(400, message);
    }

    public IllegalArgumentException(int code, Object... templateArgs) {
        super(code, null, false, templateArgs);
    }

}
