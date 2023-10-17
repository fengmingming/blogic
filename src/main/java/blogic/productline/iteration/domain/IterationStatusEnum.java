package blogic.productline.iteration.domain;

import blogic.core.enums.IDigitalizedEnum;
import blogic.core.exception.IllegalEnumValueException;

public enum IterationStatusEnum implements IDigitalizedEnum {

    NotStarted(10, "未开始"),
    InProgress(20, "进行中"),
    Completed(30, "已完成"),
    ;

    IterationStatusEnum(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    private int code;
    private String codeDesc;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getCodeDesc() {
        return codeDesc;
    }

    public static IterationStatusEnum findIterationStatusEnum(int code) {
        for(IterationStatusEnum e : IterationStatusEnum.values()) {
            if(e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalEnumValueException(IterationStatusEnum.class, code);
    }
}
