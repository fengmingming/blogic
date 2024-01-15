package blogic.user.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.user.domain.repository.UserInvitationRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("user_invitation")
public class UserInvitation extends ActiveRecord<UserInvitation, Long> {

    @Id
    private Long id;
    @Column("company_id")
    private Long companyId;
    @Column("phone")
    private String phone;
    @Column("user_id")
    private Long userId;
    @Column("roles")
    private String roles;
    @Column("departments")
    private String departments;
    @Column("status")
    private Integer status;
    @Column("create_time")
    private LocalDateTime createTime;

    @Override
    protected ReactiveCrudRepository<UserInvitation, Long> findRepository() {
        return SpringContext.getBean(UserInvitationRepository.class);
    }

    @Override
    protected <S extends UserInvitation> S selfS() {
        return (S) this;
    }

    public UserInvitationStatusEnum getStatusEnum() {
        return UserInvitationStatusEnum.findByCode(getStatus());
    }

}
