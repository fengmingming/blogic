package blogic.user.domain;

import blogic.core.enums.IDigitalizedEnum;
import blogic.core.exception.IllegalEnumValueException;

public enum UserInvitationStatusEnum implements IDigitalizedEnum {

    Inviting(10, "邀请中"),
    Agreed(90, "已同意"),
    Cancel(96, "已撤销"),
    Reject(95, "已拒绝"),
    ;

    private int code;
    private String codeDesc;

    UserInvitationStatusEnum(int code, String codeDesc) {
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

    public static UserInvitationStatusEnum findByCode(int code) {
        for(UserInvitationStatusEnum e: UserInvitationStatusEnum.values()) {
            if(e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalEnumValueException(UserInvitationStatusEnum.class, code);
    }

}
