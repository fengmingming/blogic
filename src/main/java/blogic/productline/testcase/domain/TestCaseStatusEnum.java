package blogic.productline.testcase.domain;

import blogic.core.enums.IDigitalizedEnum;

import java.util.Arrays;

public enum TestCaseStatusEnum implements IDigitalizedEnum {

    NotStarted(10, "未开始"),
    Testing(20, "测试中"),
    Blocked(30, "被阻塞"),
    Completed(40, "已完成"),
    ;

    TestCaseStatusEnum(int code, String codeDesc) {
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

    public static TestCaseStatusEnum findByCode(int code) {
        return IDigitalizedEnum.findByCode(Arrays.asList(TestCaseStatusEnum.values()), code);
    }

}
