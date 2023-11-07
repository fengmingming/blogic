package blogic.productline.testcase.rest;

import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.testcase.domain.QTestCase;
import blogic.productline.testcase.domain.TestCaseStatusEnum;
import blogic.productline.testcase.domain.TestCaseStep;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.productline.testcase.service.TestCaseService;
import blogic.user.domain.QUser;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
public class TestCaseRest {

    @Autowired
    private TestCaseRepository testCaseRepository;
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;
        @Column("createUserName")
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        private String steps;

        public Collection<TestCaseStep> getSteps() {
            return JSONUtil.toBean(this.steps, new TypeReference<List<TestCaseStep>>() {}, false);
        }

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
            QUser ownerUser = new QUser("ownerUser");
            QUser createUser = new QUser("createUser");
            Predicate predicate = qTestCase.productId.eq(productId).and(qTestCase.deleted.eq(false));
            return q.select(Projections.bean(FindTestCasesRes.class, qTestCase, ownerUser.name.as("ownerUserName"), createUser.name.as("createUserName")))
                    .from(qTestCase)
                    .leftJoin(ownerUser).on(qTestCase.ownerUserId.eq(ownerUser.id))
                    .leftJoin(createUser).on(qTestCase.createUserId.eq(createUser.id))
                    .where(predicate).orderBy(qTestCase.id.desc()).offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList().map(its -> ResVo.success(its)));
    }

    @Setter
    @Getter
    public static class CreateTestCaseReq {
        private Long iterationId;
        private Long requirementId;
        @NotBlank
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
        @Size(min = 1, max = 100)
        private List<TestCaseStep> steps;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/TestCases")
    public Mono<ResVo<?>> createTestCase(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId,
                                         TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CreateTestCaseReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTestCaseOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), null);
        TestCaseService.CreateTestCaseCommand command = new TestCaseService.CreateTestCaseCommand();
        command.setProductId(productId);
        command.setIterationId(req.getIterationId());
        command.setRequirementId(req.getRequirementId());
        command.setPriority(req.getPriority());
        command.setTitle(req.getTitle());
        command.setPrecondition(req.getPrecondition());
        command.setSmoke(req.getSmoke());
        command.setOwnerUserId(req.getOwnerUserId());
        command.setCreateUserId(tokenInfo.getUserId());
        command.setSteps(req.getSteps());
        return verifyMono.then(testCaseService.createTestCase(command)).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateTaskCaseReq implements DTOLogicConsistencyVerifier {
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
        @Size(min = 1, max = 100)
        private List<TestCaseStep> steps;
        private Long ownerUserId;
        @NotNull
        private Boolean smoke;
        @NotNull
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        private TestCaseStatusEnum status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    @PutMapping("/Companies/{companyId}/Products/{productId}/TestCases/{testCaseId}")
    public Mono<ResVo<?>> updateTestCase(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId,
                                         @PathVariable("testCaseId") Long testCaseId, TokenInfo tokenInfo, UserCurrentContext context,
                                         @RequestBody @Valid UpdateTaskCaseReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTestCaseOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), testCaseId);
        Mono<Void> ownerVerifyMono = Mono.defer(() -> {
            if(req.getOwnerUserId() != null) {
                return productLineVerifier.containsUserOrThrowException(productId, req.getOwnerUserId());
            }else {
                return Mono.empty();
            }
        });
        TestCaseService.UpdateTestCaseCommand command = new TestCaseService.UpdateTestCaseCommand();
        command.setTestCaseId(testCaseId);
        command.setRequirementId(req.getRequirementId());
        command.setIterationId(req.getIterationId());
        command.setTitle(req.getTitle());
        command.setPrecondition(req.getPrecondition());
        command.setPriority(req.getPriority());
        command.setSmoke(req.getSmoke());
        command.setSteps(req.getSteps());
        command.setOwnerUserId(req.getOwnerUserId());
        command.setStatus(req.getStatus());
        command.setCompleteTime(req.getCompleteTime());
        return verifyMono.then(ownerVerifyMono).then(testCaseService.updateTestCase(command)).then(Mono.just(ResVo.success()));
    }

}
