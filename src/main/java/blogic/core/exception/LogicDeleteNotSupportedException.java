package blogic.core.exception;

public class LogicDeleteNotSupportedException extends AbstractCodedException{

    public LogicDeleteNotSupportedException(Class clazz) {
        super(1008, clazz.getSimpleName());
    }

}
