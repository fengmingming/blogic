package blogic.productline.requirement.rest;

import blogic.core.exception.ForbiddenAccessException;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.QRequirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.requirement.domain.RequirementStatus;
import blogic.productline.requirement.service.RequirementService;
import blogic.user.domain.QUser;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class RequirementRest {

    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RequirementService requirementService;

    @Setter
    @Getter
    public static class FindRequirementReq extends Paging {
        private String requirementName;
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        private RequirementStatus requirementStatus;
        private Long createUserId;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Requirements")
    public Mono<ResVo<?>> findRequirements(@PathVariable("companyId") Long companyId,
                                           @PathVariable("productId")Long productId,
                                           TokenInfo tokenInfo, UserCurrentContext context,
                                           @RequestBody FindRequirementReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Boolean> verifyProductBelongToCompanyMono = productRepository.verifyProductBelongToCompany(productId, companyId);
        return verifyProductBelongToCompanyMono.flatMapMany(it -> {
            if(it) {
                return requirementRepository.query(query -> {
                    QRequirement qRequirement = QRequirement.requirement;
                    Predicate predicate = qRequirement.productId.eq(productId).and(qRequirement.deleted.eq(false));
                    if(StrUtil.isNotBlank(req.getRequirementName())) {
                        predicate = ExpressionUtils.and(predicate, qRequirement.requirementName.like(req.getRequirementName()));
                    }
                    if(req.getRequirementStatus() != null) {
                        predicate = ExpressionUtils.and(predicate, qRequirement.requirementStatus.eq(req.getRequirementStatus().getCode()));
                    }
                    if(req.getCreateUserId() != null) {
                        predicate = ExpressionUtils.and(predicate, qRequirement.createUserId.eq(tokenInfo.getUserId()));
                    }
                    return query.select(qRequirement)
                            .from(qRequirement)
                            .where(predicate)
                            .limit(req.getLimit()).offset(req.getOffset());
                }).all();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        }).collectList().flatMap(it -> Mono.just(ResVo.success(it)));
    }

    @Setter
    @Getter
    public static class FindRequirementRes {
        private Long id;
        private Long productId;
        private String requirementName;
        private String requirementSources;
        private String requirementDesc;
        private Integer requirementStatus;
        private Long createUserId;
        @Column("createUserName")
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Requirements/{requirementId}")
    public Mono<ResVo<?>> findRequirement(@PathVariable("companyId") Long companyId,
                                                     @PathVariable("productId")Long productId,
                                                     @PathVariable("requirementId")Long requirementId,
                                                     UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Boolean> verifyProductBelongToCompanyMono = productRepository.verifyProductBelongToCompany(productId, companyId);
        return verifyProductBelongToCompanyMono.flatMap(it -> {
            if(it) {
                QRequirement qRequirement = QRequirement.requirement;
                QUser qUser = QUser.user;
                return requirementRepository.query(query -> query.select(Projections.bean(FindRequirementRes.class, qRequirement, qUser.name.as("createUserName")))
                        .from(qRequirement).innerJoin(qUser).on(qRequirement.createUserId.eq(qUser.id))
                        .where(qRequirement.id.eq(requirementId).and(qRequirement.productId.eq(productId)))
                ).one();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        }).flatMap(it -> Mono.just(ResVo.success(it)));
    }

    @Setter
    @Getter
    public static class CreateRequirementReq {
        @NotBlank
        @Length(max = 254)
        private String requirementName;
        @Length(max = 254)
        private String requirementSources;
        private String requirementDesc;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/Requirements")
    public Mono<Long> createRequirement(@PathVariable("companyId") Long companyId,
                                        @PathVariable("productId")Long productId,
                                        TokenInfo tokenInfo, UserCurrentContext context,
                                        @RequestBody @Valid CreateRequirementReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMap(it -> {
            if(it) {
                RequirementService.CreateRequirementCommand command = new RequirementService.CreateRequirementCommand();
                command.setProductId(productId);
                command.setRequirementName(req.getRequirementName());
                command.setRequirementSources(req.getRequirementSources());
                command.setRequirementDesc(req.getRequirementDesc());
                command.setCreateUserId(tokenInfo.getUserId());
                return requirementService.createRequirement(command).map(r -> r.getId());
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

    @Setter
    @Getter
    public static class UpdateRequirementReq {
        @NotBlank
        @Length(max = 254)
        private String requirementName;
        @Length(max = 254)
        private String requirementSources;
        private String requirementDesc;
    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Requirements/{requirementId}")
    public Mono<Void> updateRequirement(@PathVariable("companyId") Long companyId,
                                        @PathVariable("productId")Long productId,
                                        @PathVariable("requirementId")Long requirementId,
                                        TokenInfo tokenInfo, UserCurrentContext context,
                                        @RequestBody @Valid UpdateRequirementReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        }).then(requirementRepository.findById(requirementId).flatMap(it -> {
            if(it.getCreateUserId().equals(tokenInfo.getUserId())) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        })).then(Mono.fromSupplier(() -> {
            RequirementService.UpdateRequirementCommand command = new RequirementService.UpdateRequirementCommand();
            command.setRequirementId(requirementId);
            command.setRequirementName(req.getRequirementName());
            command.setRequirementSources(req.getRequirementName());
            command.setRequirementDesc(req.getRequirementDesc());
            return requirementService.updateRequirement(command);
        })).then();
    }

    @DeleteMapping("/Companies/{companyId}/Products/{productId}/Requirements/{requirementId}")
    public Mono<Void> deleteRequirement(@PathVariable("companyId") Long companyId,
                                        @PathVariable("productId")Long productId,
                                        @PathVariable("requirementId")Long requirementId,
                                        TokenInfo tokenInfo, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productRepository.verifyProductBelongToCompany(productId, companyId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        }).then(requirementRepository.findById(requirementId).flatMap(it -> {
            if(it.getCreateUserId().equals(tokenInfo.getUserId())) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        })).then(requirementService.deleteRequirement(requirementId)).then();
    }


}
