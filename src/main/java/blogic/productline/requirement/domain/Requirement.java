package blogic.productline.requirement.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
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
@Table("requirement")
public class Requirement extends ActiveRecord<Requirement, Long> {

    @Id
    private Long id;
    @Column("product_id")
    private Long productId;
    @Column("requirement_name")
    private String requirementName;
    @Column("requirement_sources")
    private String requirementSources;
    @Column("requirement_desc")
    private String requirementDesc;
    @Column("requirement_status")
    private Integer requirementStatus;
    @Column("create_user_id")
    private Long createUserId;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    public RequirementStatus getRequirementStatusEnum() {
        if(this.requirementStatus == null) return null;
        return RequirementStatus.findByCode(this.requirementStatus);
    }

    public void setRequirementStatusEnum(RequirementStatus requirementStatus) {
        this.requirementStatus = requirementStatus.getCode();
    }

    @Override
    protected ReactiveCrudRepository<Requirement, Long> findRepository() {
        return SpringContext.getBean(RequirementRepository.class);
    }

    @Override
    protected Requirement selfT() {
        return this;
    }

}
