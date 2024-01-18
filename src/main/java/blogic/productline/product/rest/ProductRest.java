package blogic.productline.product.rest;

import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.product.service.ProductService;
import blogic.user.common.UserDto;
import blogic.user.domain.QUser;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductRest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductMemberRepository productMemberRepository;

    @Setter
    @Getter
    public static class FindProductReq extends Paging{
        private Long createUserId;
        private String productName;
    }

    @Setter
    @Getter
    public static class FindProductRes {
        private Long id;
        private Long companyId;
        private String productName;
        private String productDesc;
        private Long createUserId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
        @Column("createUserName")
        private String createUserName;
    }

    @GetMapping("/Companies/{companyId}/Products")
    public Mono<ResVo<?>> findProducts(@PathVariable("companyId") Long companyId, TokenInfo tokenInfo,
                                       UserCurrentContext context, FindProductReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        if (context.authenticate(RoleEnum.ROLE_MANAGER)) {
            QProduct qProduct = QProduct.product;
            QUser qUser = QUser.user;
            Predicate predicate = qProduct.companyId.eq(companyId).and(qProduct.deleted.eq(false));
            if(StrUtil.isNotBlank(req.getProductName())) {
                predicate = ExpressionUtils.and(predicate, qProduct.productName.like("%" + req.getProductName() + "%"));
            }
            if(req.getCreateUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qProduct.createUserId.eq(req.getCreateUserId()));
            }
            Predicate predicateFinal = predicate;
            Mono<List<FindProductRes>> productListMono = productRepository.query(query -> query.select(Projections.bean(FindProductRes.class, qProduct, qUser.name.as("createUserName")))
                    .from(qProduct)
                    .leftJoin(qUser).on(qProduct.createUserId.eq(qUser.id))
                    .where(predicateFinal)
                    .orderBy(qProduct.createTime.desc())
                    .offset(req.getOffset()).limit(req.getLimit())).all().collectList();
            Mono<Long> productTotalMono = productRepository.query(query -> query.select(qProduct.id.count())
                    .from(qProduct)
                    .leftJoin(qUser).on(qProduct.createUserId.eq(qUser.id))
                    .where(predicateFinal)).one();
            return Mono.zip(productListMono, productTotalMono).map(t -> ResVo.success(t.getT2(), t.getT1()));
        } else {
            QProduct qProduct = QProduct.product;
            QProductMember qPm = QProductMember.productMember;
            QUser qUser = QUser.user;
            Predicate predicate = qProduct.deleted.isFalse();
            if(StrUtil.isNotBlank(req.getProductName())) {
                predicate = ExpressionUtils.and(predicate, qProduct.productName.like("%" + req.getProductName() + "%"));
            }
            Predicate predicateFinal = predicate;
            Mono<List<FindProductRes>> productListMono = productRepository.query(query -> query.select(Projections.bean(FindProductRes.class, qProduct, qUser.name.as("createUserName")))
                    .from(qProduct)
                    .innerJoin(qPm).on(qProduct.id.eq(qPm.productId).and(qPm.userId.eq(tokenInfo.getUserId())).and(qProduct.companyId.eq(companyId)))
                    .innerJoin(qUser).on(qUser.id.eq(qProduct.createUserId))
                    .where(predicateFinal)
                    .orderBy(qProduct.createTime.desc())
                    .offset(req.getOffset()).limit(req.getLimit())).all().collectList();
            Mono<Long> productTotalMono = productRepository.query(query -> query.select(qProduct.id.count())
                    .from(qProduct)
                    .innerJoin(qPm).on(qProduct.id.eq(qPm.productId).and(qPm.userId.eq(tokenInfo.getUserId())).and(qProduct.companyId.eq(companyId)))
                    .innerJoin(qUser).on(qUser.id.eq(qProduct.createUserId))
                    .where(predicateFinal)).one();
            return Mono.zip(productListMono, productTotalMono).map(t -> ResVo.success(t.getT2(), t.getT1()));
        }
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}")
    public Mono<ResVo<?>> findById(@PathVariable("companyId") Long companyId, @PathVariable("productId") Long productId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        QProduct qProduct = QProduct.product;
        Mono<ProductDto> productMono = productRepository.query(q -> q.select(Projections.bean(ProductDto.class, qProduct)).from(qProduct).where(qProduct.id.eq(productId).and(qProduct.companyId.eq(companyId)))).one();
        QProductMember qPM = QProductMember.productMember;
        QUser qUser = QUser.user;
        Mono<List<UserDto>> usersMono = productMemberRepository.query(q -> q.select(Projections.bean(UserDto.class, qUser)).from(qPM).innerJoin(qUser).on(qPM.userId.eq(qUser.id)).where(qPM.productId.eq(productId))).all().collectList();
        return Mono.zip(productMono, usersMono).map(tuple2 -> {
            ProductDto productDto = tuple2.getT1();
            List<UserDto> users = tuple2.getT2();
            productDto.setUsers(users);
            return ResVo.success(productDto);
        });
    }

    @Setter
    @Getter
    public static class CreateProductReq {
        @NotBlank
        @Length(max = 254)
        private String productName;
        private String productDesc;
        private List<Long> userIds = new ArrayList<>();
    }

    @PostMapping("/Companies/{companyId}/Products")
    public Mono<ResVo<?>> createProduct(@PathVariable("companyId") Long companyId, TokenInfo tokenInfo,
                                        UserCurrentContext context, @RequestBody @Valid CreateProductReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_PM);
        if(CollectionUtil.isEmpty(req.getUserIds()) || !req.getUserIds().contains(tokenInfo.getUserId())) {
            req.getUserIds().add(tokenInfo.getUserId());
        }
        ProductService.CreateProductCommand command = new ProductService.CreateProductCommand();
        command.setCreateUserId(tokenInfo.getUserId());
        command.setProductName(req.getProductName());
        command.setProductDesc(req.getProductDesc());
        command.setCompanyId(companyId);
        command.setMembers(req.getUserIds());
        return productService.createProduct(command).flatMap(it -> Mono.just(ResVo.success(it)));
    }

    @Setter
    @Getter
    public static class UpdateProductReq {
        @NotBlank
        @Length(max = 254)
        private String productName;
        private String productDesc;
        private List<Long> userIds;
    }

    @PutMapping("/Companies/{companyId}/Products/{productId}")
    public Mono<ResVo<?>> updateProduct(@PathVariable("companyId") Long companyId, @PathVariable("productId")Long productId,
                                        TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid UpdateProductReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_PM);
        if(CollectionUtil.isEmpty(req.getUserIds()) || !req.getUserIds().contains(tokenInfo.getUserId())) {
            req.getUserIds().add(tokenInfo.getUserId());
        }
        ProductService.UpdateProductCommand command = new ProductService.UpdateProductCommand();
        command.setProductId(productId);
        command.setProductName(req.getProductName());
        command.setProductDesc(req.getProductDesc());
        command.setMembers(req.getUserIds());
        return productRepository.findById(productId).filter(it -> it.getCreateUserId().equals(tokenInfo.getUserId()))
                .switchIfEmpty(Mono.error(new ForbiddenAccessException()))
                .then(productService.updateProduct(command).then(Mono.just(ResVo.success())));
    }

    @DeleteMapping("/Companies/{companyId}/Products/{productId}")
    public Mono<ResVo<?>> deleteProduct(@PathVariable("companyId") Long companyId,
                                        @PathVariable("productId")Long productId,
                                        TokenInfo tokenInfo, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_PM);
        return productRepository.findById(productId).filter(it -> it.getCreateUserId().equals(tokenInfo.getUserId()))
                .switchIfEmpty(Mono.error(new ForbiddenAccessException()))
                .then(productService.deleteProduct(productId))
                .then(Mono.just(ResVo.success()));
    }

}
