package blogic.core.security;

import blogic.core.exception.AbstractCodedException;

public class NotSelectUserCurrentContextException extends AbstractCodedException {

    public NotSelectUserCurrentContextException() {
        super(1006);
    }

}
