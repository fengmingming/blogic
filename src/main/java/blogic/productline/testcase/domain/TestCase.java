package blogic.productline.testcase.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
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
@Table("test_case")
public class TestCase extends ActiveRecord<TestCase, Long> {

    @Id
    private Long id;
    @Column("iteration_id")
    private Long iterationId;
    @Column("requirement_id")
    private Long requirementId;
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

}
