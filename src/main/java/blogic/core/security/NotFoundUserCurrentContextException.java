package blogic.core.security;

import blogic.core.rest.CodedException;

public class NotFoundUserCurrentContextException extends RuntimeException implements CodedException {

    @Override
    public int getCode() {
        return 1003;
    }

}
