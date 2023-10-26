package blogic.productline.bug.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.core.domain.LogicConsistencyException;
import blogic.core.domain.LogicConsistencyProcessor;
import blogic.productline.bug.domain.repository.BugRepository;
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
@Table("bug")
public class Bug extends ActiveRecord<Bug, Long> implements LogicConsistencyProcessor {

    @Id
    private Long id;
    @Column("test_case_id")
    private Long testCaseId;
    @Column("requirement_id")
    private Long requirementId;
    @Column("iteration_id")
    private Long iterationId;
    @Column("product_id")
    private Long productId;
    @Column("iteration_version")
    private String iteration_version;
    @Column("title")
    private String title;
    @Column("bug_type")
    private Integer bugType;
    @Column("env")
    private Integer env;
    @Column("device")
    private String device;
    @Column("repro_steps")
    private String reproSteps;
    @Column("status")
    private Integer status;
    @Column("severity")
    private Integer severity;
    @Column("priority")
    private Integer priority;
    @Column("current_user_id")
    private Long currentUserId;
    @Column("fix_user_id")
    private Long fixUserId;
    @Column("fix_solution")
    private String fixSolution;
    @Column("fix_version")
    private String fixVersion;
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

    @Override
    protected ReactiveCrudRepository<Bug, Long> findRepository() {
        return SpringContext.getBean(BugRepository.class);
    }

    @Override
    protected <S extends Bug> S selfS() {
        return (S) this;
    }

    @Override
    public void verifyLogicConsistency() throws LogicConsistencyException {
        
    }

    public void setStatusEnum(BugStatusEnum status) {
        if(status != null) {
            setStatus(status.getCode());
        }
    }

    public BugStatusEnum getStatusEnum() {
        if(getStatus() == null) return null;
        return BugStatusEnum.findByCode(getStatus());
    }

}
