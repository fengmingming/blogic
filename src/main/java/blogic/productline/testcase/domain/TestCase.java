package blogic.productline.testcase.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
@Table("test_case")
public class TestCase extends ActiveRecord<TestCase, Long> {

    @Id
    private Long id;
    @Column("iteration_id")
    private Long iterationId;
    @Column("requirement_id")
    private Long requirementId;
    @Column("product_id")
    private Long productId;
    @Column("title")
    private String title;
    @Column("priority")
    private Integer priority;
    @Column("precondition")
    private String precondition;
    @Column("steps")
    @Setter(AccessLevel.NONE)
    private String steps;
    @Column("owner_user_id")
    private Long ownerUserId;
    @Column("smoke")
    @NotNull
    private Boolean smoke;
    @Column("status")
    private Integer status;
    @Column("complete_time")
    private LocalDateTime completeTime;
    @Column("create_user_id")
    private Long createUserId;
    @Column("create_time")
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<TestCase, Long> findRepository() {
        return SpringContext.getBean(TestCaseRepository.class);
    }

    @Override
    protected <S extends TestCase> S selfS() {
        return (S) this;
    }

    public void setStatusEnum(TestCaseStatusEnum statusEnum) {
        if(statusEnum != null) setStatus(statusEnum.getCode());
    }

    public TestCaseStatusEnum getStatusEnum() {
        if(getStatus() == null) return null;
        return TestCaseStatusEnum.findByCode(getStatus());
    }

    public Collection<TestCaseStep> getSteps() {
        List<TestCaseStep> steps = JSONUtil.toBean(this.steps, new TypeReference<List<TestCaseStep>>() {}, false);
        if(steps == null) return null;
        return Collections.unmodifiableList(steps);
    }

    public void setSteps(Collection<TestCaseStep> steps) {
        this.steps = JSONUtil.toJsonStr(steps);
    }

}
