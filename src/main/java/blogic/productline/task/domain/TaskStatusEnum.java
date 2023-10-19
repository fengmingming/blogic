package blogic.productline.task.domain;

import blogic.core.enums.IDigitalizedEnum;
import lombok.Getter;

@Getter
public enum TaskStatusEnum implements IDigitalizedEnum {

    NotStarted(10, "未开始"),
    InProgress(20, "进行中"),
    Completed(30, "已完成"),
    Canceled(40, "已取消"),
    ;

    TaskStatusEnum(int code,String codeDesc) {
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

}
