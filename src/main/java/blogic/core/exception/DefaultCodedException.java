package blogic.core.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class DefaultCodedException extends AbstractCodedException {

    public DefaultCodedException(int code, Object ... args) {
        super(code, args);
    }

    public static DefaultCodedException build(int code, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException(code, args);
        return codedException;
    }

}
