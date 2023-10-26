package blogic.productline.testcase.service;

import blogic.core.exception.IllegalArgumentException;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.testcase.domain.TestCase;
import blogic.productline.testcase.domain.TestCaseStatusEnum;
import blogic.productline.testcase.domain.TestCaseStep;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@Validated
public class TestCaseService {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Setter
    @Getter
    public static class CreateTestCaseCommand {
        private Long iterationId;
        private Long requirementId;
        @NotNull
        private Long productId;
        @NotNull
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer priority;
        @Length(max = 1000)
        private String precondition;
        private Long ownerUserId;
        @NotNull
        private Boolean smoke;
        @NotNull
        private Long createUserId;
        @NotNull
        @Size(min = 1, max = 100)
        private List<TestCaseStep> steps;
    }

    @Transactional
    public Mono<TestCase> createTestCase(@NotNull @Valid CreateTestCaseCommand command) {
        TestCase testCase = new TestCase();
        testCase.setProductId(command.getProductId());
        testCase.setIterationId(command.getIterationId());
        testCase.setRequirementId(command.getRequirementId());
        testCase.setStatusEnum(TestCaseStatusEnum.NotStarted);
        testCase.setTitle(command.getTitle());
        testCase.setPriority(command.getPriority());
        testCase.setPrecondition(command.getPrecondition());
        testCase.setOwnerUserId(command.getOwnerUserId());
        testCase.setSmoke(command.getSmoke());
        testCase.setCreateUserId(command.getCreateUserId());
        testCase.setCreateTime(LocalDateTime.now());
        return testCaseRepository.save(testCase);
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateTestCaseCommand implements DTOLogicConsistencyVerifier {
        @NotNull
        private Long testCaseId;
        private Long iterationId;
        private Long requirementId;
        @NotBlank
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer priority;
        @Length(max = 1000)
        private String precondition;
        @NotNull
        @Size(max = 100)
        private List<TestCaseStep> steps;
        private Long ownerUserId;
        @NotNull
        private Boolean smoke;
        @NotNull
        private TestCaseStatusEnum status;
        private LocalDateTime completeTime;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            if(this.status == TestCaseStatusEnum.Completed && this.completeTime == null) {
                throw new IllegalArgumentException("completeTime is null");
            }
            if(this.status != TestCaseStatusEnum.NotStarted && this.ownerUserId == null) {
                throw new IllegalArgumentException("ownerUserId is null");
            }
        }
    }

    @Transactional
    public Mono<TestCase> updateTestCase(@NotNull @Valid UpdateTestCaseCommand command) {
        Mono<TestCase> testCaseMono = testCaseRepository.findById(command.getTestCaseId());
        Function<TestCase, Mono<TestCase>> saveTestCase = (it -> {
            it.setIterationId(command.getIterationId());
            it.setRequirementId(command.getRequirementId());
            it.setStatusEnum(command.getStatus());
            it.setTitle(command.getTitle());
            it.setPrecondition(command.getPrecondition());
            it.setPriority(command.getPriority());
            it.setSmoke(command.getSmoke());
            it.setOwnerUserId(command.getOwnerUserId());
            it.setSteps(command.getSteps());
            it.setCompleteTime(command.getCompleteTime());
            it.setUpdateTime(LocalDateTime.now());
            return testCaseRepository.save(it);
        });
        return testCaseMono.flatMap(saveTestCase);
    }

}
