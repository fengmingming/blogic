package blogic.core.security;

import blogic.core.exception.AbstractCodedException;

public class UnauthorizedException extends AbstractCodedException {

    public UnauthorizedException() {
        super(401);
    }

}
