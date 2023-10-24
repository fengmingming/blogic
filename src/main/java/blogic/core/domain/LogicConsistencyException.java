package blogic.core.domain;

import blogic.core.exception.CodedException;

public class LogicConsistencyException extends RuntimeException implements CodedException {

    public LogicConsistencyException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return 1007;
    }

    @Override
    public Object[] getTemplateArgs() {
        return new Object[]{getMessage()};
    }

}
