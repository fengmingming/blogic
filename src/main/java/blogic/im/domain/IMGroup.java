package blogic.im.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.im.domain.repository.IMGroupRepository;
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
@Table("im_group")
public class IMGroup extends ActiveRecord<IMGroup, Long> {

    @Id
    private Long id;
    @Column("group_name")
    private String groupName;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<IMGroup, Long> findRepository() {
        return SpringContext.getBean(IMGroupRepository.class);
    }

    @Override
    protected <S extends IMGroup> S selfS() {
        return (S) this;
    }

}
