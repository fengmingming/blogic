package blogic.user.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("user_company_role")
public class UserCompanyRole extends ActiveRecord<UserCompanyRole, Long> {

    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("company_id")
    private Long companyId;
    @Column("role")
    private String role;
    @Column("admin")
    private Boolean admin;
    @Column("create_time")
    private LocalDateTime createTime;

    public RoleEnum getRoleEnum() {
        return RoleEnum.valueOf(getRole());
    }

    @Override
    protected ReactiveCrudRepository<UserCompanyRole, Long> findRepository() {
        return SpringContext.getBean(UserCompanyRoleRepository.class);
    }

    @Override
    protected <S extends UserCompanyRole> S selfS() {
        return (S) this;
    }

}
