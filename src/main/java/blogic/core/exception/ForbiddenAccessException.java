package blogic.core.exception;

public class ForbiddenAccessException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 403;
    }

}
