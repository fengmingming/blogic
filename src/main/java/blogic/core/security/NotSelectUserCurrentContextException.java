package blogic.core.security;

import blogic.core.exception.CodedException;

public class NotSelectUserCurrentContextException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 1006;
    }

}
