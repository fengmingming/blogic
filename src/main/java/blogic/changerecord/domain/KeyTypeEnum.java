package blogic.changerecord.domain;

import blogic.core.enums.IDigitalizedEnum;

public enum KeyTypeEnum implements IDigitalizedEnum {

    Project(8, "项目"),
    Requirement(9, "需求"),
    Iteration(10, "迭代"),
    TestCase(11, "用例"),
    Task(12, "任务"),
    Bug(13, "缺陷");

    private int code;
    private String codeDesc;

    KeyTypeEnum(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getCodeDesc() {
        return this.codeDesc;
    }
}
