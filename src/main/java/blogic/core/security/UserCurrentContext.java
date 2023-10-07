package blogic.core.security;

import blogic.core.exception.ForbiddenAccessException;
import blogic.user.domain.RoleEnum;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCurrentContext implements Serializable {

    private String token;
    private Long companyId;
    private String companyName;
    private List<RoleEnum> authorities = new ArrayList<>();

    public boolean equalCompanyId(long companyId) {
        return this.equals(companyId);
    }

    public void equalCompanyIdAndThrowException(long companyId) {
        if(!equalCompanyId(companyId)) throw new ForbiddenAccessException();
    }

}
