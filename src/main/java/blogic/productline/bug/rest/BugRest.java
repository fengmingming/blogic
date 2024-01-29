package blogic.productline.bug.rest;

import blogic.core.enums.DigitalizedEnumPropertyEditor;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.bug.domain.BugStatusEnum;
import blogic.productline.bug.domain.QBug;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.bug.mapstruct.BugMapStruct;
import blogic.productline.bug.service.BugService;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class BugRest {

    @Autowired
    private BugRepository bugRepository;
    @Autowired
    private BugService bugService;
    @Autowired
    private ProductLineVerifier productLineVerifier;
    @Autowired
    private BugMapStruct bugMapStruct;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private TestCaseRepository testCaseRepository;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(BugStatusEnum.class, new DigitalizedEnumPropertyEditor(BugStatusEnum.class));
    }

    @Setter
    @Getter
    public static class FindBugsReq extends Paging {
        private Long id;
        private Long testCaseId;
        private Long iterationId;
        private Long requirementId;
        private BugStatusEnum status;
        private Long fixUserId;
        private Long currentUserId;
        private String title;
        private Integer bugType;
        private Integer env;
    }

    @Getter
    @Setter
    public static class FindBugRes {
        private Long id;
        private Long testCaseId;
        private String testCaseTitle;
        private Long requirementId;
        private String requirementName;
        private Long iterationId;
        private String iterationName;
        private Long productId;
        private String iterationVersion;
        private String title;
        private Integer bugType;
        private Integer env;
        private String device;
        private String reproSteps;
        private Integer status;
        private Integer severity;
        private Integer priority;
        private Long currentUserId;
        private String currentUserName;
        private Long fixUserId;
        private String fixUserName;
        private Integer fixSolution;
        private String fixVersion;
        private Long createUserId;
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Bugs")
    public Mono<ResVo<?>> findBugs(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                   TokenInfo tokenInfo, UserCurrentContext context, FindBugsReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        QBug qBug = QBug.bug;
        Predicate predicate = qBug.deleted.isFalse().and(qBug.productId.eq(productId));
        if(req.getIterationId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.iterationId.eq(req.getIterationId()));
        }
        if(req.getRequirementId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.requirementId.eq(req.getRequirementId()));
        }
        if(req.getTestCaseId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.testCaseId.eq(req.getTestCaseId()));
        }
        if(req.getStatus() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.status.eq(req.getStatus().getCode()));
        }
        if(req.getCurrentUserId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.currentUserId.eq(req.getCurrentUserId()));
        }
        if(req.getFixUserId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.fixUserId.eq(req.getFixUserId()));
        }
        if(StrUtil.isNotBlank(req.getTitle())) {
            predicate = ExpressionUtils.and(predicate, qBug.title.like("%" + req.getTitle() + "%"));
        }
        if(req.getEnv() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.env.eq(req.getEnv()));
        }
        if(req.getBugType() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.bugType.eq(req.getBugType()));
        }
        Predicate predicateFinal = predicate;
        Mono<List<FindBugRes>> records = bugRepository.query(q -> {
            return q.select(Projections.bean(FindBugRes.class, qBug))
                    .from(qBug).where(predicateFinal).orderBy(qBug.id.desc()).offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList();
        Function<List<FindBugRes>, Mono<List<FindBugRes>>> setUsers = (bugs) -> {
            Set<Long> userIds = new HashSet<>();
            userIds.addAll(bugs.stream().map(it -> it.getCreateUserId()).collect(Collectors.toSet()));
            userIds.addAll(bugs.stream().map(it -> it.getCurrentUserId()).filter(Objects::nonNull).collect(Collectors.toSet()));
            userIds.addAll(bugs.stream().map(it -> it.getFixUserId()).filter(Objects::nonNull).collect(Collectors.toSet()));
            if(userIds.size() > 0) {
                return userRepository.findByIdsAndToMap(userIds).map(map -> {
                    bugs.forEach(bug -> {
                        bug.setCreateUserName(map.get(bug.getCreateUserId()));
                        bug.setCurrentUserName(map.get(bug.getCurrentUserId()));
                        bug.setFixUserName(map.get(bug.getFixUserId()));
                    });
                    return bugs;
                });
            }
            return Mono.just(bugs);
        };
        Function<List<FindBugRes>, Mono<List<FindBugRes>>> setIterations = (bugs) -> {
            Collection<Long> iterationIds = bugs.stream().map(it -> it.getIterationId()).filter(Objects::nonNull).collect(Collectors.toSet());
            if(iterationIds.size() > 0) {
                return iterationRepository.findByIdsAndToMap(iterationIds).map(map -> {
                    bugs.forEach(bug -> {
                        bug.setIterationName(map.get(bug.getIterationId()));
                    });
                    return bugs;
                });
            }
            return Mono.just(bugs);
        };
        Function<List<FindBugRes>, Mono<List<FindBugRes>>> setRequirements = (bugs) -> {
            Collection<Long> requirementIds = bugs.stream().map(it -> it.getRequirementId()).filter(Objects::nonNull).collect(Collectors.toSet());
            if(requirementIds.size() > 0) {
                return requirementRepository.findByIdsAndToMap(requirementIds).map(map -> {
                    bugs.forEach(bug -> {
                        bug.setRequirementName(map.get(bug.getRequirementId()));
                    });
                    return bugs;
                });
            }
            return Mono.just(bugs);
        };
        Function<List<FindBugRes>, Mono<List<FindBugRes>>> setTestCases = (bugs) -> {
            Collection<Long> testCaseIds = bugs.stream().map(it -> it.getTestCaseId()).filter(Objects::nonNull).collect(Collectors.toSet());
            if(testCaseIds.size() > 0) {
                return testCaseRepository.findByIdsAndToMap(testCaseIds).map(map -> {
                    bugs.forEach(bug -> {
                        bug.setTestCaseTitle(map.get(bug.getTestCaseId()));
                    });
                    return bugs;
                });
            }
            return Mono.just(bugs);
        };
        Mono<Long> total = bugRepository.query(q -> {
            return q.select(qBug.id.count()).from(qBug).where(predicateFinal);
        }).one();
        return productLineVerifier.verifyProductOrThrowException(companyId, productId)
                .then(Mono.zip(total, records.flatMap(setUsers).flatMap(setIterations).flatMap(setRequirements).flatMap(setTestCases)
                ).map(it -> ResVo.success(it.getT1(), it.getT2())));
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Bugs/{bugId}")
    public Mono<ResVo<?>> findBugs(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                   @PathVariable("bugId")Long bugId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        Function<FindBugRes, Mono<FindBugRes>> setUser = (bug) -> {
            Set<Long> userIds = new HashSet<>();
            if(bug.getCreateUserId() != null) {
                userIds.add(bug.getCreateUserId());
            }
            if(bug.getCurrentUserId() != null) {
                userIds.add(bug.getCurrentUserId());
            }
            if(bug.getFixUserId() != null) {
                userIds.add(bug.getFixUserId());
            }
            if(userIds.size() > 0) {
                return userRepository.findByIdsAndToMap(userIds).map(map -> {
                    bug.setCreateUserName(map.get(bug.getCreateUserId()));
                    bug.setCurrentUserName(map.get(bug.getCurrentUserId()));
                    bug.setFixUserName(map.get(bug.getFixUserId()));
                    return bug;
                });
            }
            return Mono.just(bug);
        };
        Function<FindBugRes, Mono<FindBugRes>> setIteration = (bug) -> {
            if(bug.getIterationId() != null) {
                return iterationRepository.findById(bug.getIterationId()).map(iteration -> {
                    bug.setIterationName(iteration.getName());
                    return bug;
                });
            }
            return Mono.just(bug);
        };
        Function<FindBugRes, Mono<FindBugRes>> setRequirement = (bug) -> {
            if(bug.getRequirementId() != null) {
                return requirementRepository.findById(bug.getRequirementId()).map(requirement -> {
                    bug.setRequirementName(requirement.getRequirementName());
                    return bug;
                });
            }
            return Mono.just(bug);
        };
        Function<FindBugRes, Mono<FindBugRes>> setTestCase = (bug) -> {
            if(bug.getTestCaseId() != null) {
                return testCaseRepository.findById(bug.getTestCaseId()).map(testCase -> {
                    bug.setTestCaseTitle(testCase.getTitle());
                    return bug;
                });
            }
            return Mono.just(bug);
        };
        QBug qBug = QBug.bug;
        return verifyMono.then(bugRepository.query(q -> {
            return q.select(Projections.bean(FindBugRes.class, qBug)).from(qBug).where(qBug.id.eq(bugId));
        }).one().flatMap(setUser).flatMap(setIteration).flatMap(setRequirement).flatMap(setTestCase).map(it -> ResVo.success(it)));
    }

    @Setter
    @Getter
    public static class CreateBugReq {
        private Long testCaseId;
        private Long requirementId;
        private Long iterationId;
        @Length(max = 50)
        private String iterationVersion;
        @NotBlank
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer env;
        @Length(max = 254)
        private String device;
        @Length(max = 1000)
        private String reproSteps;
        @NotNull
        private Integer severity;
        @NotNull
        private Integer priority;
        private Long currentUserId;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/Bugs")
    public Mono<ResVo<?>> createBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                    TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CreateBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), req.getTestCaseId(), null);
        BugService.CreateBugCommand command = bugMapStruct.mapToCommand(req);
        command.setProductId(productId);
        command.setCreateUserId(tokenInfo.getUserId());
        return verifyMono.then(bugService.createBug(command)).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateBugReq implements DTOLogicConsistencyVerifier {
        private Long testCaseId;
        private Long requirementId;
        private Long iterationId;
        @Length(max = 50)
        private String iterationVersion;
        @NotBlank
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer env;
        @Length(max = 254)
        private String device;
        private String reproSteps;
        @NotNull
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        private BugStatusEnum status;
        private Integer severity;
        private Integer priority;
        private Long currentUserId;
        private Integer fixSolution;
        @Length(max = 50)
        private String fixVersion;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            if(status == BugStatusEnum.Activated || status == BugStatusEnum.Confirmed
                    || status == BugStatusEnum.solved) {
                if(currentUserId == null) {
                    throw new IllegalArgumentException("UpdateBugReq.currentUserId is null");
                }
            }
            if(status == BugStatusEnum.solved) {
                if(fixSolution == null) {
                    throw new IllegalArgumentException("UpdateBugReq.fixSolution is null");
                }
            }
        }
    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Bugs/{bugId}")
    public Mono<ResVo<?>> updateBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("bugId") Long bugId,
                                    TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid UpdateBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        BugService.UpdateBugCommand command = bugMapStruct.mapToCommand(req);
        command.setBugId(bugId);
        if(req.getStatus() == BugStatusEnum.solved) {
            command.setFixUserId(tokenInfo.getUserId());
        }
        return verifyMono.then(bugService.updateBug(command)).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class ConfirmBugReq {
        @NotNull
        private Long currentUserId;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer priority;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Bugs/{bugId}", params = "action=confirmBug")
    public Mono<ResVo<?>> confirmBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("bugId") Long bugId,
                                     TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid ConfirmBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        return verifyMono.then(Mono.defer(() -> {
            BugService.ConfirmBugCommand command = new BugService.ConfirmBugCommand();
            command.setBugId(bugId);
            command.setCurrentUserId(req.getCurrentUserId());
            command.setBugType(req.getBugType());
            command.setPriority(req.getPriority());
            command.setRemark(req.getRemark());
            return bugService.confirm(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class AppointBugReq {
        @NotNull
        private Long currentUserId;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Bugs/{bugId}", params = "action=appointBug")
    public Mono<ResVo<?>> appointBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("bugId") Long bugId,
                                     TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid AppointBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        return verifyMono.then(Mono.defer(() -> {
            BugService.AppointBugCommand command = new BugService.AppointBugCommand();
            command.setBugId(bugId);
            command.setCurrentUserId(req.getCurrentUserId());
            command.setRemark(req.getRemark());
            return bugService.appoint(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class CloseBugReq {
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Bugs/{bugId}", params = "action=closeBug")
    public Mono<ResVo<?>> closeBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("bugId") Long bugId,
                                     TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CloseBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        return verifyMono.then(Mono.defer(() -> {
            BugService.CloseBugCommand command = new BugService.CloseBugCommand();
            command.setBugId(bugId);
            command.setRemark(req.getRemark());
            return bugService.closeBug(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class FixBugReq {
        @NotNull
        private Integer fixSolution;
        @Length(max = 50)
        private String fixVersion;
        @NotNull
        private LocalDateTime fixTime;
        @NotNull
        private Long currentUserId;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Bugs/{bugId}", params = "action=fixBug")
    public Mono<ResVo<?>> fixBug(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("bugId") Long bugId,
                                   TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid FixBugReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        return verifyMono.then(Mono.defer(() -> {
            BugService.FixBugCommand command = new BugService.FixBugCommand();
            command.setBugId(bugId);
            command.setCurrentUserId(req.getCurrentUserId());
            command.setFixSolution(req.getFixSolution());
            command.setFixVersion(req.getFixVersion());
            command.setFixTime(req.getFixTime());
            command.setRemark(req.getRemark());
            return bugService.fixBug(command);
        })).then(Mono.just(ResVo.success()));
    }

}
