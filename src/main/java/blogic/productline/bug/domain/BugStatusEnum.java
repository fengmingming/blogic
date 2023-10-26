package blogic.productline.bug.domain;

import blogic.core.enums.IDigitalizedEnum;

import java.util.Arrays;

public enum BugStatusEnum implements IDigitalizedEnum {

    Activated(10, "已激活"),
    solved(20, "已解决"),
    Closed(30, "已关闭"),
    ;

    private int code;
    private String codeDesc;

    BugStatusEnum(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getCodeDesc() {
        return null;
    }

    public static BugStatusEnum findByCode(int code) {
        return IDigitalizedEnum.findByCode(Arrays.asList(BugStatusEnum.values()), code);
    }

}
