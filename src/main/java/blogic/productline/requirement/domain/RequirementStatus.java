package blogic.productline.requirement.domain;

import blogic.core.enums.IDigitalizedEnum;
import blogic.core.exception.IllegalEnumValueException;
import lombok.Getter;

@Getter
public enum RequirementStatus implements IDigitalizedEnum {

    Confirmed(10, "已确认"),
    Scheduled(20, "已规划"),
    InDevelopment(30, "开发中"),
    Implemented(40, "已实现"),
    Released(50, "已发布"),
    Closed(60, "已关闭"),
    ;

    private int code;
    private String codeDesc;

    RequirementStatus(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    public static RequirementStatus findByCode(int code) {
        for(RequirementStatus e : RequirementStatus.values()) {
            if(e.code == code) {
                return e;
            }
        }
        throw new IllegalEnumValueException(RequirementStatus.class, code);
    }

}
