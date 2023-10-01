package blogic.user.service;

import blogic.user.domain.RoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UserCompanyDto {

    private Long userId;
    private Long companyId;
    private String companyName;
    private boolean admin;
    private List<RoleEnum> roles = new ArrayList<>();

}
