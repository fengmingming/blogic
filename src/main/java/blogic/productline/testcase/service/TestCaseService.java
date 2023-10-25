package blogic.productline.testcase.service;

import blogic.productline.testcase.domain.TestCase;
import blogic.productline.testcase.domain.TestCaseStatusEnum;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.productline.testcase.rest.TestCaseStepDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
public class TestCaseService {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Setter
    @Getter
    public static class CreateTestCaseCommand{
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
        private List<TestCaseStepDto> steps;
    }

    public Mono<TestCase> createTestCase(@Valid CreateTestCaseCommand command) {
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
        testCaseRepository.save(testCase).flatMap(it -> {
            return Mono.just(it);
        });
        return null;
    }

}
