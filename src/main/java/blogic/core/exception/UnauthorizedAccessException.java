package blogic.core.exception;

public class UnauthorizedAccessException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 403;
    }

}
