package blogic.productline.bug.rest;

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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

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

    @Setter
    @Getter
    public static class FindBugsReq extends Paging {
        private Long testCaseId;
        private Long iterationId;
        private Long requirementId;
        private BugStatusEnum status;
        private Long createUserId;
        private Long fixUserId;
        private Long currentUserId;
        private String title;
    }

    @Getter
    @Setter
    public static class FindBugRes {
        private Long id;
        private Long testCaseId;
        private Long requirementId;
        private Long iterationId;
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
        private Long fixUserId;
        private Integer fixSolution;
        private String fixVersion;
        private Long createUserId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
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
        if(req.getCreateUserId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.createUserId.eq(req.getCreateUserId()));
        }
        if(req.getCurrentUserId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.currentUserId.eq(req.getCurrentUserId()));
        }
        if(req.getFixUserId() != null) {
            predicate = ExpressionUtils.and(predicate, qBug.fixUserId.eq(req.getFixUserId()));
        }
        if(StrUtil.isNotBlank(req.getTitle())) {
            predicate = ExpressionUtils.and(predicate, qBug.title.like(req.getTitle()));
        }
        Predicate predicateFinal = predicate;
        Mono<List<FindBugRes>> records = bugRepository.query(q -> {
            return q.select(Projections.bean(FindBugRes.class, qBug))
                    .from(qBug).where(predicateFinal).orderBy(qBug.id.desc()).offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList();
        Mono<Long> total = bugRepository.query(q -> {
            return q.select(qBug.id.count()).from(qBug).where(predicateFinal);
        }).one();
        return productLineVerifier.verifyProductOrThrowException(companyId, productId)
                .then(Mono.zip(total, records).map(it -> ResVo.success(it.getT1(), it.getT2())));
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Bugs/{bugId}")
    public Mono<ResVo<?>> findBugs(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                   @PathVariable("bugId")Long bugId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyBugOrThrowException(companyId, productId, null, null, null, bugId);
        QBug qBug = QBug.bug;
        return verifyMono.then(bugRepository.query(q -> {
            return q.select(Projections.bean(FindBugRes.class, qBug)).from(qBug).where(qBug.id.eq(bugId));
        }).one().map(it -> ResVo.success(it)));
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

}
