package blogic.productline.iteration.rest;

import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.infras.MyDataReq;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.iteration.domain.IterationStatusEnum;
import blogic.productline.iteration.domain.QIteration;
import blogic.productline.iteration.domain.QIterationMember;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.iteration.service.IterationService;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.QRequirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.user.common.UserDto;
import blogic.user.domain.QUser;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IterationRest {

    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private IterationMemberRepository iterationMemberRepository;
    @Autowired
    private IterationService iterationService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private ProductLineVerifier productLineVerifier;

    @Setter
    @Getter
    public static class FindIterationReq extends Paging {
        private Long createUserId;
        private String name;
        private Integer status;
    }

    @Setter
    @Getter
    public static class FindIterationRes {
        private Long id;
        private Long productId;
        private String productName;
        private String versionCode;
        private String name;
        private LocalDate scheduledStartTime;
        private LocalDate scheduledEndTime;
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
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMap(it -> {
            if(it) {
                QIteration qi = QIteration.iteration;
                QProduct qp = QProduct.product;
                QUser qu = QUser.user;
                Predicate predicate = qi.productId.eq(productId).and(qi.deleted.eq(false));
                if(req.getCreateUserId() != null) {
                    predicate = ExpressionUtils.and(predicate, qi.createUserId.eq(req.getCreateUserId()));
                }
                if(req.getStatus() != null) {
                    predicate = ExpressionUtils.and(predicate, qi.status.eq(req.getStatus()));
                }
                if(StrUtil.isNotBlank(req.getName())) {
                    predicate = ExpressionUtils.and(predicate, qi.name.like("%" + req.getName() + "%"));
                }
                Predicate predicateFinal = predicate;
                Mono<List<FindIterationRes>> records = iterationRepository.query(q -> {
                    return q.select(Projections.bean(FindIterationRes.class, qi, qp.productName, qu.name.as("createUserName")))
                            .from(qi)
                            .innerJoin(qp).on(qi.productId.eq(qp.id))
                            .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                            .where(predicateFinal)
                            .orderBy(qi.createTime.desc())
                            .limit(req.getLimit()).offset(req.getOffset());
                }).all().collectList();
                Mono<Long> total = iterationRepository.query(q -> {
                    return q.select(qi.id.count())
                            .from(qi)
                            .innerJoin(qp).on(qi.productId.eq(qp.id))
                            .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                            .where(predicateFinal);
                }).one();
                return Mono.zip(total, records);
            }else {
               return Mono.error(new ForbiddenAccessException());
            }
        }).map(it -> ResVo.success(it.getT1(), it.getT2()));
    }

    @GetMapping("/Companies/{companyId}/Products/Iteration")
    public Mono<ResVo<?>> findMyIteration(@PathVariable("companyId")Long companyId, TokenInfo tokenInfo,
                                          UserCurrentContext context, @Valid MyDataReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        tokenInfo.equalsUserIdOrThrowException(req.getCreateUserId());
        QIteration qi = QIteration.iteration;
        QProduct qp = QProduct.product;
        QUser qu = QUser.user;
        Predicate predicate = qi.deleted.eq(false).and(qi.createUserId.eq(req.getCreateUserId()));
        final Predicate predicateFinal = predicate;
        Mono<List<FindIterationRes>> records = iterationRepository.query(q -> {
            return q.select(Projections.bean(FindIterationRes.class, qi, qp.productName, qu.name.as("createUserName")))
                    .from(qi)
                    .innerJoin(qp).on(qi.productId.eq(qp.id))
                    .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                    .where(predicateFinal)
                    .orderBy(qi.createTime.desc())
                    .limit(req.getLimit()).offset(req.getOffset());
        }).all().collectList();
        Mono<Long> total = iterationRepository.query(q -> {
            return q.select(qi.id.count())
                    .from(qi)
                    .innerJoin(qp).on(qi.productId.eq(qp.id))
                    .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                    .where(predicateFinal);
        }).one();
        return Mono.zip(total, records).map(it -> ResVo.success(it.getT1(), it.getT2()));
    }

    @Setter
    @Getter
    public static class FindOneIterationRes extends FindIterationRes {
        private List<UserDto> users;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Iteration/{iterationId}")
    public Mono<ResVo<?>> findById(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("iterationId")Long iterationId) {
        Mono<FindOneIterationRes> iterationMono = iterationRepository.query(query -> {
            QIteration qi = QIteration.iteration;
            QProduct qp = QProduct.product;
            QUser qu = QUser.user;
            return query.select(Projections.bean(FindOneIterationRes.class, qi, qp.productName, qu.name.as("createUserName")))
                    .from(qi)
                    .innerJoin(qp).on(qi.productId.eq(qp.id))
                    .innerJoin(qu).on(qi.createUserId.eq(qu.id))
                    .where(qi.id.eq(iterationId));
        }).one();
        QIterationMember qIM = QIterationMember.iterationMember;
        QUser qUser = QUser.user;
        Mono<List<UserDto>> usersMono = iterationMemberRepository.query(q -> q.select(Projections.bean(UserDto.class, qUser)).from(qIM).innerJoin(qUser).on(qIM.userId.eq(qUser.id)).where(qIM.iterationId.eq(iterationId))).all().collectList();
        return productLineVerifier.verifyIterationOrThrowException(companyId, productId, iterationId)
        .then(Mono.zip(iterationMono, usersMono).map(it -> {
            it.getT1().setUsers(it.getT2());
            return ResVo.success(it.getT1());
        }));
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
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate scheduledStartTime;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate scheduledEndTime;
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
                if(CollectionUtil.isNotEmpty(req.getUserIds())) {
                    usersMono = usersMono.map(its -> new ArrayList<>(CollectionUtil.intersection(its, req.getUserIds())));
                }
                Mono<List<Long>> requirementsMono = Mono.just(Collections.EMPTY_LIST);
                if(CollectionUtil.isNotEmpty(req.getRequirementIds())) {
                    requirementsMono = requirementRepository.query(q -> q.select(QRequirement.requirement.id).from(QRequirement.requirement)
                            .where(QRequirement.requirement.id.in(req.getRequirementIds()).and(QRequirement.requirement.productId.eq(productId))))
                            .all().collectList();
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
        private IterationStatusEnum status;
        private LocalDate scheduledStartTime;
        private LocalDate scheduledEndTime;
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
                Mono<List<Long>> requirementsMono = Mono.just(Collections.emptyList());
                if(CollectionUtil.isNotEmpty(req.getRequirementIds())) {
                    requirementsMono = requirementRepository.query(q -> q.select(QRequirement.requirement.id).from(QRequirement.requirement)
                                    .where(QRequirement.requirement.id.in(req.getRequirementIds()).and(QRequirement.requirement.productId.eq(productId))))
                            .all().collectList();
                }
                return Mono.zip(usersMono, requirementsMono).flatMap(tuple2 -> {
                    IterationService.UpdateIterationCommand command = new IterationService.UpdateIterationCommand();
                    command.setIterationId(iterationId);
                    command.setName(req.getName());
                    command.setVersionCode(req.getVersionCode());
                    command.setScheduledStartTime(req.getScheduledStartTime());
                    command.setScheduledEndTime(req.getScheduledEndTime());
                    command.setUserIds(tuple2.getT1());
                    command.setRequirementIds(tuple2.getT2());
                    command.setStatus(req.getStatus());
                    return iterationService.updateIteration(command);
                });
            })).then(Mono.just(ResVo.success()));
    }

}
