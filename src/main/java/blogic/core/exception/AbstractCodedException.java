package blogic.core.exception;

import blogic.core.context.SpringContext;

import java.util.Locale;

public abstract class AbstractCodedException extends RuntimeException implements CodedException {

    private int code;
    private Object[] templateArgs;

    public AbstractCodedException(int code, Object ... templateArgs) {
        super(SpringContext.getMessage(code, Locale.getDefault(), String.format("service exception [%d]", code), templateArgs));
        this.code = code;
        this.templateArgs = templateArgs;
    }

    public AbstractCodedException(int code, Throwable throwable, Object ... templateArgs) {
        super(SpringContext.getMessage(code, Locale.getDefault(), String.format("service exception [%d]", code), templateArgs), throwable);
        this.code = code;
        this.templateArgs = templateArgs;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public Object[] getTemplateArgs() {
        return templateArgs;
    }

}
