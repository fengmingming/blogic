package blogic.core.exception;

public class IllegalArgumentException extends RuntimeException implements CodedException{

    public IllegalArgumentException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return 400;
    }

    @Override
    public Object[] getTemplateArgs() {
        return new Object[]{getMessage()};
    }

}
