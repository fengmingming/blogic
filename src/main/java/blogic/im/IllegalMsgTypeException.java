package blogic.im;

import blogic.core.exception.AbstractCodedException;

public class IllegalMsgTypeException extends AbstractCodedException {

    public IllegalMsgTypeException(MsgType msgType) {
        super(1004, msgType.name());
    }

}
