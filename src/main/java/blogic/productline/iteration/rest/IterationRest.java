package blogic.productline.iteration.rest;

import blogic.core.exception.ForbiddenAccessException;
import blogic.core.json.DigitalizedEnumDeserializer;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.iteration.domain.IterationStatusEnum;
import blogic.productline.iteration.domain.QIteration;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.iteration.service.IterationService;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.QRequirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.user.domain.QUser;
import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IterationRest {

    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private IterationService iterationService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RequirementRepository requirementRepository;

    @Setter
    @Getter
    public static class FindIterationReq extends Paging {
        private Long createUserId;
    }

    @Setter
    @Getter
    public static class FindIterationRes {
        private Long id;
        private Long productId;
        private String productName;
        private String versionCode;
        private String name;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private Integer status;
        private Long createUserId;
        @Column("createUserName")
        private String createUserName;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Iteration")
    public Mono<ResVo<?>> findIteration(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                        TokenInfo tokenInfo, UserCurrentContext context, FindIterationReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        if(req.getCreateUserId() != null) {
            tokenInfo.equalsUserIdOrThrowException(req.getCreateUserId());
        }
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMapMany(it -> {
            if(it) {
                QIteration qi = QIteration.iteration;
                QProduct qp = QProduct.product;
                QUser qu = QUser.user;
                return iterationRepository.query(q -> {
                    Predicate predicate = qi.productId.eq(productId).and(qi.deleted.eq(false));
                    if(req.getCreateUserId() != null) {
                        predicate = ExpressionUtils.and(predicate, qi.createUserId.eq(req.getCreateUserId()));
                    }
                    return q.select(Projections.bean(FindIterationRes.class, qi, qp.productName, qu.name.as("createUserName")))
                            .from(qi)
                            .innerJoin(qp).on(qi.productId.eq(qp.id))
                            .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                            .where(predicate)
                            .orderBy(qi.createTime.desc())
                            .limit(req.getLimit()).offset(req.getOffset());
                }).all();
            }else {
               return Mono.error(new ForbiddenAccessException());
            }
        }).collectList().flatMap(it -> Mono.just(ResVo.success(it)));
    }

    @Setter
    @Getter
    public static class CreateIterationReq {
        @NotNull
        @Length(max = 50)
        private String versionCode;
        @NotNull
        @Length(max = 254)
        private String name;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private List<Long> userIds;
        private List<Long> requirementIds;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/Iteration")
    public Mono<ResVo<?>> createIteration(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                          TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CreateIterationReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMap(it -> {
            if(it) {
                Mono<List<Long>> usersMono = productRepository.findById(productId).flatMapMany(p -> p.findMembers()).collectList().map(its -> its.stream().map(u -> u.getUserId()).collect(Collectors.toList()));
                Mono<List<Long>> requirementsMono = requirementRepository.query(q -> q.select(QRequirement.requirement.id)
                        .where(QRequirement.requirement.id.in(req.getRequirementIds()).and(QRequirement.requirement.productId.eq(productId))))
                        .all().collectList();
                if(CollectionUtil.isNotEmpty(req.getUserIds())) {
                    usersMono = usersMono.map(its -> new ArrayList<>(CollectionUtil.intersection(its, req.getUserIds())));
                }
                return Mono.zip(usersMono, requirementsMono).flatMap(tuple2 -> {
                    List<Long> userIds = tuple2.getT1();
                    List<Long> requirementIds = tuple2.getT2();
                    IterationService.CreateIterationCommand command = new IterationService.CreateIterationCommand();
                    command.setProductId(productId);
                    command.setName(req.getName());
                    command.setVersionCode(req.getVersionCode());
                    command.setScheduledStartTime(req.getScheduledStartTime());
                    command.setScheduledEndTime(req.getScheduledEndTime());
                    command.setCreateUserId(tokenInfo.getUserId());
                    command.setUserIds(userIds);
                    command.setRequirementIds(requirementIds);
                    return iterationService.createIteration(command);
                });
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        }).flatMap(it -> Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class UpdateIterationReq {
        @NotNull
        @Length(max = 50)
        private String versionCode;
        @NotNull
        @Length(max = 254)
        private String name;
        @NotNull
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        private IterationStatusEnum iterationStatus;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        @NotNull
        @Size(min = 1)
        private List<Long> userIds;
        private List<Long> requirementIds;
    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Iteration/{iterationId}")
    public Mono<ResVo<?>> updateIteration(@PathVariable("companyId")Long companyId,
                                          @PathVariable("productId")Long productId,
                                          @PathVariable("iterationId")Long iterationId,
                                          UserCurrentContext context,
                                          @RequestBody @Valid UpdateIterationReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId)
            .then(iterationRepository.verifyIterationBelongToProductOrThrowException(iterationId, productId))
            .then(Mono.defer(() -> {
                Mono<List<Long>> usersMono = productRepository.findById(productId).flatMapMany(p -> p.findMembers()).collectList().map(its -> its.stream().map(u -> u.getUserId()).collect(Collectors.toList()));
                if(CollectionUtil.isNotEmpty(req.getUserIds())) {
                    usersMono = usersMono.map(its -> new ArrayList<>(CollectionUtil.intersection(its, req.getUserIds())));
                }
                Mono<List<Long>> requirementsMono = requirementRepository.query(q -> q.select(QRequirement.requirement.id)
                                .where(QRequirement.requirement.id.in(req.getRequirementIds()).and(QRequirement.requirement.productId.eq(productId))))
                        .all().collectList();
                return Mono.zip(usersMono, requirementsMono).flatMap(tuple2 -> {
                    IterationService.UpdateIterationCommand command = new IterationService.UpdateIterationCommand();
                    command.setIterationId(iterationId);
                    command.setName(req.getName());
                    command.setVersionCode(req.getVersionCode());
                    command.setScheduledStartTime(req.getScheduledStartTime());
                    command.setScheduledEndTime(req.getScheduledEndTime());
                    command.setUserIds(tuple2.getT1());
                    command.setRequirementIds(tuple2.getT2());
                    return iterationService.updateIteration(command);
                });
            })).then(Mono.just(ResVo.success()));
    }

}
