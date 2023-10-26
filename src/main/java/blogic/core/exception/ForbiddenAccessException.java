package blogic.core.exception;

public class ForbiddenAccessException extends AbstractCodedException {

    public ForbiddenAccessException() {
        super(403);
    }

}
