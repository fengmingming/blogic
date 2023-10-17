package blogic.productline.iteration.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.iteration.domain.repository.IterationRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("iteration")
public class Iteration extends ActiveRecord<Iteration, Long> {

    @Id
    private Long id;
    @Column("product_id")
    @NotNull
    private Long productId;
    @Column("version_code")
    @NotNull
    @Length(max = 50)
    private String versionCode;
    @Column("name")
    @NotNull
    @Length(max = 254)
    private String name;
    @Column("scheduled_start_time")
    private LocalDateTime scheduledStartTime;
    @Column("scheduled_end_time")
    private LocalDateTime scheduledEndTime;
    @Column("status")
    @NotNull
    private Integer status;
    @Column("create_user_id")
    @NotNull
    private Long createUserId;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    public void setStatusEnum(IterationStatusEnum statusEnum) {
        if(statusEnum == null) return;
        this.setStatus(statusEnum.getCode());
    }

    public IterationStatusEnum getStatusEnum() {
        Integer status = getStatus();
        if(status == null) return null;
        return IterationStatusEnum.findIterationStatusEnum(status);
    }

    @Override
    protected ReactiveCrudRepository<Iteration, Long> findRepository() {
        return SpringContext.getBean(IterationRepository.class);
    }

    @Override
    protected <S extends Iteration> S selfS() {
        return (S) this;
    }

}
