package blogic.core.exception;

public class BExecConstraintException extends AbstractCodedException {

    public BExecConstraintException(int code, Object... templateArgs) {
        super(code, null, false, templateArgs);
    }

    public BExecConstraintException(Object templateArg) {
        super(1007, null, false, new Object[]{templateArg});
    }

}
