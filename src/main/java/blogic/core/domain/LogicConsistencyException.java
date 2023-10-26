package blogic.core.domain;

import blogic.core.exception.AbstractCodedException;

public class LogicConsistencyException extends AbstractCodedException {

    public LogicConsistencyException(String message) {
        super(1007, message);
    }

}
