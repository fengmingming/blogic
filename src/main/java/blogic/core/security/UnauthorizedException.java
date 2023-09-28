package blogic.core.security;

import blogic.core.CodedException;

public class UnauthorizedException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 401;
    }

}
