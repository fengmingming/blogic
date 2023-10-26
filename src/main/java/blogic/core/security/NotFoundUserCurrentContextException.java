package blogic.core.security;

import blogic.core.exception.AbstractCodedException;

public class NotFoundUserCurrentContextException extends AbstractCodedException {

    public NotFoundUserCurrentContextException() {
        super(1003);
    }

}
