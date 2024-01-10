package blogic.user.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDepartmentDto {

    private Long userId;
    private Long departmentId;
    private String departmentName;

}
