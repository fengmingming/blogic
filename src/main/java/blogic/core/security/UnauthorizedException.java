package blogic.core.security;

import blogic.core.exception.CodedException;

public class UnauthorizedException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 401;
    }

}
