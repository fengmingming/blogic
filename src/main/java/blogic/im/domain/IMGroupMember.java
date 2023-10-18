package blogic.im.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.im.domain.repository.IMGroupMemberRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("im_group_member")
public class IMGroupMember extends ActiveRecord<IMGroupMember, Long> {

    @Id
    private Long id;
    @Column("group_id")
    private Long groupId;
    @Column("user_id")
    private Long userId;
    @Column("admin")
    private Boolean admin;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<IMGroupMember, Long> findRepository() {
        return SpringContext.getBean(IMGroupMemberRepository.class);
    }

    @Override
    protected <S extends IMGroupMember> S selfS() {
        return (S) this;
    }
}
