package blogic.productline.testcase.rest;

import blogic.core.enums.DigitalizedEnumPropertyEditor;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.iteration.domain.Iteration;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.requirement.domain.Requirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.testcase.domain.QTestCase;
import blogic.productline.testcase.domain.TestCaseStatusEnum;
import blogic.productline.testcase.domain.TestCaseStep;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.productline.testcase.service.TestCaseService;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.ExpressionUtils;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class TestCaseRest {

    @Autowired
    private TestCaseRepository testCaseRepository;
    @Autowired
    private ProductLineVerifier productLineVerifier;
    @Autowired
    private TestCaseService testCaseService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private RequirementRepository requirementRepository;

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
        private String iterationName;
        private Long requirementId;
        private String requirementName;
        private Long productId;
        private String title;
        private Integer priority;
        private String precondition;
        private Long ownerUserId;
        @Column("ownerUserName")
        private String ownerUserName;
        private Boolean smoke;
        private Integer status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;
        private Long createUserId;
        @Column("createUserName")
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
        private String steps;

        public Collection<TestCaseStep> getSteps() {
            return JSONUtil.toBean(this.steps, new TypeReference<List<TestCaseStep>>() {}, false);
        }

    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(TestCaseStatusEnum.class, new DigitalizedEnumPropertyEditor(TestCaseStatusEnum.class));
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
        QTestCase qTestCase = QTestCase.testCase;
        Predicate predicate = qTestCase.productId.eq(productId).and(qTestCase.deleted.eq(false));
        if(req.getStatus() != null) {
            predicate = ExpressionUtils.and(predicate, qTestCase.status.eq(req.getStatus().getCode()));
        }
        Predicate predicateFinal = predicate;
        Mono<List<FindTestCasesRes>> records = testCaseRepository.query(q -> {
            return q.select(Projections.bean(FindTestCasesRes.class, qTestCase))
                    .from(qTestCase)
                    .where(predicateFinal).orderBy(qTestCase.id.desc())
                    .offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList();
        Mono<Long> total = testCaseRepository.query(q -> {
            return q.select(qTestCase.id.count())
                    .from(qTestCase)
                    .where(predicateFinal);
        }).one();
        Function<List<FindTestCasesRes>, Mono<List<FindTestCasesRes>>> setUserMono = (its) -> {
            Set<Long> userIds = new HashSet<>();
            userIds.addAll(its.stream().map(it -> it.getOwnerUserId()).filter(it -> it != null).collect(Collectors.toSet()));
            userIds.addAll(its.stream().map(it -> it.getCreateUserId()).filter(it -> it != null).collect(Collectors.toSet()));
            if(userIds.size() > 0) {
                return userRepository.findAllById(userIds).collectList().map(users -> {
                    Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
                    its.stream().forEach(it -> {
                        it.setOwnerUserName(userMap.get(it.getOwnerUserId()));
                        it.setCreateUserName(userMap.get(it.getCreateUserId()));
                    });
                    return its;
                });
            }
            return Mono.just(its);
        };
        Function<List<FindTestCasesRes>, Mono<List<FindTestCasesRes>>> setRequirementMono = (its) -> {
            Collection<Long> requirementIds = its.stream().map(it -> it.getRequirementId()).filter(it -> it != null).collect(Collectors.toSet());
            if(requirementIds.size() > 0) {
                return requirementRepository.findAllById(requirementIds).collectList().map(requirements -> {
                    Map<Long, String> requirementMap = requirements.stream().collect(Collectors.toMap(Requirement::getId, Requirement::getRequirementName));
                    its.stream().forEach(it -> {
                        it.setRequirementName(requirementMap.get(it.getRequirementId()));
                    });
                    return its;
                });
            }
            return Mono.just(its);
        };
        Function<List<FindTestCasesRes>, Mono<List<FindTestCasesRes>>> setIterationMono = (its) -> {
            Collection<Long> iterationIds = its.stream().map(it -> it.getIterationId()).filter(it -> it != null).collect(Collectors.toSet());
            if(iterationIds.size() > 0) {
                return iterationRepository.findAllById(iterationIds).collectList().map(iteration -> {
                    Map<Long, String> iterationMap = iteration.stream().collect(Collectors.toMap(Iteration::getId, Iteration::getName));
                    its.stream().forEach(it -> {
                        it.setIterationName(iterationMap.get(it.getIterationId()));
                    });
                    return its;
                });
            }
            return Mono.just(its);
        };
        return verifyMono.then(Mono.zip(total, records.flatMap(setUserMono).flatMap(setRequirementMono).flatMap(setIterationMono)).map(it -> ResVo.success(it.getT1(), it.getT2())));
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/TestCases/{testCaseId}")
    public Mono<ResVo<?>> findTestCases(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId, @PathVariable("testCaseId") Long testCaseId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTestCaseOrThrowException(companyId, productId, null, null, testCaseId);
        Function<FindTestCasesRes, Mono<FindTestCasesRes>> setUserMono = (it) -> {
            Set<Long> userIds = new HashSet<>();
            if(it.getOwnerUserId() != null) {
                userIds.add(it.getOwnerUserId());
            }
            if(it.getCreateUserId() != null) {
                userIds.add(it.getCreateUserId());
            }
            if(userIds.size() > 0) {
                return userRepository.findAllById(userIds).collectList().map(users -> {
                    Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
                    it.setOwnerUserName(userMap.get(it.getOwnerUserId()));
                    it.setCreateUserName(userMap.get(it.getCreateUserId()));
                    return it;
                });
            }
            return Mono.just(it);
        };
        Function<FindTestCasesRes, Mono<FindTestCasesRes>> setRequirementMono = (it) -> {
            if(it.getRequirementId() != null) {
                return requirementRepository.findById(it.getRequirementId()).map(requirement -> {
                    it.setRequirementName(requirement.getRequirementName());
                    return it;
                });
            }
            return Mono.just(it);
        };
        Function<FindTestCasesRes, Mono<FindTestCasesRes>> setIterationMono = (it) -> {
            if(it.getIterationId() != null) {
                return iterationRepository.findById(it.getIterationId()).map(iteration -> {
                    it.setIterationName(iteration.getName());
                    return it;
                });
            }
            return Mono.just(it);
        };
        QTestCase qTestCase = QTestCase.testCase;
        return verifyMono.then(testCaseRepository.query(q -> {
            return q.select(Projections.bean(FindTestCasesRes.class, qTestCase))
                    .from(qTestCase)
                    .where(qTestCase.id.eq(testCaseId));
        }).one().flatMap(setUserMono).flatMap(setRequirementMono).flatMap(setIterationMono).map(it -> ResVo.success(it)));
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
