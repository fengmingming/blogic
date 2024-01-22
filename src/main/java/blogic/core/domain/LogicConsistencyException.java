package blogic.core.domain;

import blogic.core.exception.AbstractCodedException;

public class LogicConsistencyException extends AbstractCodedException {

    public LogicConsistencyException(Object message) {
        super(1007, message);
    }

}
