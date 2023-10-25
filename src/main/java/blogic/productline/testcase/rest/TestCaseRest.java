package blogic.productline.testcase.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.testcase.domain.QTestCase;
import blogic.productline.testcase.domain.TestCaseStatusEnum;
import blogic.productline.testcase.domain.TestCaseStep;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.productline.testcase.domain.repository.TestCaseStepRepository;
import blogic.productline.testcase.service.TestCaseService;
import blogic.user.domain.QUser;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TestCaseRest {

    @Autowired
    private TestCaseRepository testCaseRepository;
    @Autowired
    private TestCaseStepRepository stepRepository;
    @Autowired
    private ProductLineVerifier productLineVerifier;
    @Autowired
    private TestCaseService testCaseService;

    @Setter
    @Getter
    public static class FindTestCasesReq extends Paging {
        private Long ownerUserId;
        private Long createUserId;
        private TestCaseStatusEnum status;
        private Long iterationId;
        private Long requirementId;
        private String title;
    }

    @Setter
    @Getter
    public static class FindTestCasesRes {
        private Long id;
        private Long iterationId;
        private Long requirementId;
        private Long productId;
        private String title;
        private Integer priority;
        private String precondition;
        @Column("ownerUserName")
        private String ownerUserName;
        private Boolean smoke;
        private Integer status;
        private LocalDateTime completeTime;
        @Column("createUserName")
        private String createUserName;
        private LocalDateTime createTime;
        private Collection<TestCaseStepDto> steps;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/TestCases")
    public Mono<ResVo<?>> findTestCases(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId,
                                        TokenInfo tokenInfo, UserCurrentContext context, FindTestCasesReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        if(req.getOwnerUserId() != null) {
            tokenInfo.equalsUserIdOrThrowException(req.getOwnerUserId());
        }
        if(req.getCreateUserId() != null) {
            tokenInfo.equalsUserIdOrThrowException(req.getCreateUserId());
        }
        Mono<Void> verifyMono = productLineVerifier.verifyTestCaseOrThrowException(companyId, productId, req.getRequirementId(),
                req.getIterationId(), null);
        return verifyMono.then(testCaseRepository.query(q -> {
            QTestCase qTestCase = QTestCase.testCase;
            QUser ownerUser = QUser.user;
            QUser createUser = QUser.user;
            Predicate predicate = qTestCase.productId.eq(productId).and(qTestCase.deleted.eq(false));
            return q.select(Projections.bean(FindTestCasesRes.class, qTestCase, ownerUser.name.as("ownerUserName"), createUser.name.as("createUserName")))
                    .from(qTestCase)
                    .leftJoin(ownerUser).on(qTestCase.ownerUserId.eq(ownerUser.id))
                    .leftJoin(createUser).on(qTestCase.createUserId.eq(createUser.id))
                    .where(predicate).offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList().flatMap(its -> {
            return stepRepository.findAllByTestCaseId(its.stream().map(it -> it.getId()).collect(Collectors.toList())).collectList()
                .flatMap(steps -> {
                    Map<Long, List<TestCaseStep>> stepMap = steps.stream().collect(Collectors.groupingBy(TestCaseStep::getTestCaseId));
                    its.stream().forEach(it -> {
                        List<TestCaseStep> stepList = stepMap.get(it.getId());
                        if(stepList != null) {
                            it.setSteps(stepList.stream().map(step -> {
                                TestCaseStepDto dto = new TestCaseStepDto();
                                dto.setId(step.getId());
                                dto.setNumber(step.getNumber());
                                dto.setStep(step.getStep());
                                dto.setExpectedResult(step.getExpectedResult());
                                return dto;
                            }).collect(Collectors.toList()));
                        }
                    });
                    return Mono.just(ResVo.success(its));
                });
        }));
    }

    @Setter
    @Getter
    public static class CreateTestCaseReq {
        private Long iterationId;
        private Long requirementId;
        private String title;
        private Integer priority;
        private String precondition;
        private Long ownerUserId;
        private Boolean smoke;
        @NotNull
        @Size(min = 1, max = 100)
        private List<TestCaseStepDto> steps;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/TestCases")
    public Mono<ResVo<?>> createTestCase(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId,
                                         TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CreateTestCaseReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTestCaseOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), null);
        return null;
    }


}
