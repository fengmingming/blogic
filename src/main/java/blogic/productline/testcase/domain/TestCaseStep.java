package blogic.productline.testcase.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.testcase.domain.repository.TestCaseStepRepository;
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
@Table("test_case_step")
public class TestCaseStep extends ActiveRecord<TestCaseStep, Long> {

    @Id
    private Long id;
    @Column("test_case_id")
    private Long testCaseId;
    @Column("number")
    private String number;
    @Column("step")
    private String step;
    @Column("expected_result")
    private String expectedResult;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<TestCaseStep, Long> findRepository() {
        return SpringContext.getBean(TestCaseStepRepository.class);
    }

    @Override
    protected <S extends TestCaseStep> S selfS() {
        return (S) this;
    }

}
