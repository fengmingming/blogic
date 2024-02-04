package blogic.core.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class DefaultCodedException extends AbstractCodedException {

    public DefaultCodedException(int code, Object ... args) {
        super(code, args);
    }

    public DefaultCodedException(int code, Throwable t, Object ... args) {
        super(code, t, args);
    }

    public DefaultCodedException(int code, Throwable t, boolean writableStackTrace, Object... args) {
        super(code, t, writableStackTrace, args);
    }

    public static DefaultCodedException build(int code, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException(code, args);
        return codedException;
    }

    public static DefaultCodedException build(int code, Throwable t, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException(code, t, args);
        return codedException;
    }

    public static DefaultCodedException build(int code, Throwable t, boolean writableStackTrace, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException(code, t, writableStackTrace, args);
        return codedException;
    }

    public static DefaultCodedException build(int code, boolean writableStackTrace, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException(code, null, writableStackTrace, args);
        return codedException;
    }

}
