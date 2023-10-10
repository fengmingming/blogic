package blogic.core.security;

import blogic.core.exception.ForbiddenAccessException;
import blogic.user.domain.RoleEnum;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean equalsCompanyId(long companyId) {
        verifyContextSelected();
        return this.companyId.equals(companyId);
    }

    public void equalsCompanyIdOrThrowException(long companyId) {
        if(!equalsCompanyId(companyId)) throw new ForbiddenAccessException();
    }

    public boolean authenticate(RoleEnum role) {
        return authenticate(true, role);
    }

    public void authenticateOrThrowException(RoleEnum role) {
        authenticateOrThrowException(true, role);
    }

    public boolean authenticate(boolean and, RoleEnum ... roles) {
        verifyContextSelected();
        if(and) {
            return this.authorities.containsAll(Arrays.asList(roles));
        }else {
            for(RoleEnum role : roles) {
                if(this.authorities.contains(role)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void authenticateOrThrowException(boolean and, RoleEnum ... roles) {
        if(!authenticate(and, roles)) {
            throw new ForbiddenAccessException();
        }
    }

    private void verifyContextSelected() {
        if(this.companyId == null) {
            throw new NotSelectUserCurrentContextException();
        }
    }

}
