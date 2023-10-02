package blogic.im;

import blogic.core.exception.CodedException;

public class IllegalMsgTypeException extends RuntimeException implements CodedException {

    private MsgType msgType;

    public IllegalMsgTypeException(MsgType msgType) {
        this.msgType = msgType;
    }

    @Override
    public int getCode() {
        return 1004;
    }

    @Override
    public Object[] getTemplateArgs() {
        return new Object[]{this.msgType.name()};
    }

}
