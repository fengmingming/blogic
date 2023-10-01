package blogic.user.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_company_role")
@Setter
@Getter
public class UserCompanyRole {

    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("company_id")
    private Long companyId;
    @Column("role")
    private RoleEnum role;
    @Column("admin")
    private Boolean admin;

}
