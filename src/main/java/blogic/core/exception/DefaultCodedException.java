package blogic.core.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DefaultCodedException extends RuntimeException implements CodedException{

    private int code;
    private Object[] templateArgs = new Object[]{};

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public Object[] getTemplateArgs() {
        return templateArgs;
    }

    public static DefaultCodedException build(int code, Object ... args) {
        DefaultCodedException codedException = new DefaultCodedException();
        codedException.setCode(code);
        if(args != null) {
            codedException.setTemplateArgs(args);
        }
        return codedException;
    }

}
