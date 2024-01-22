package blogic.core.exception;

public class InnerServerException extends AbstractCodedException{

    public InnerServerException(Object templateArg) {
        super(500, templateArg);
    }

    public InnerServerException(int code, Object... templateArgs) {
        super(code, null, false, templateArgs);
    }

    public InnerServerException(Throwable throwable, Object templateArg) {
        super(500, throwable, templateArg);
    }

    public InnerServerException(int code, Throwable throwable, Object... templateArgs) {
        super(code, throwable, false, templateArgs);
    }

}
