package blogic.productline.testcase.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.core.enums.IDigitalizedEnum;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.productline.testcase.domain.repository.TestCaseStepRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

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

    public Mono<Void> saveSteps(Collection<TestCaseStep> steps) {
        SpringContext.getBean(TestCaseStepRepository.class).saveAll(steps);
        return null;
    }

    public Flux<TestCaseStep> findSteps() {
        if(getId() == null) return Flux.empty();
        return SpringContext.getBean(TestCaseStepRepository.class).findAllByTestCaseId(getId());
    }

}
