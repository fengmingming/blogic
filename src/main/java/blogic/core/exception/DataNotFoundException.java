package blogic.core.exception;

public class DataNotFoundException extends RuntimeException implements CodedException{

    @Override
    public int getCode() {
        return 404;
    }

}
