package blogic.core.security;

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

}
