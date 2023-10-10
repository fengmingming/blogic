package blogic.productline.product.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.product.service.ProductService;
import blogic.user.domain.QUser;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.List;

@RestController
public class ProductRest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;

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
        @Column("createUserName")
        private String createUserName;
    }

    @GetMapping("/Companies/{companyId}/Products")
    public Mono<ResVo<?>> findProducts(@PathVariable("companyId") Long companyId, TokenInfo tokenInfo,
                                       UserCurrentContext context, @RequestBody Paging paging) {
        context.equalsCompanyIdOrThrowException(companyId);
        if (context.authenticate(RoleEnum.ROLE_MANAGER)) {
            QProduct qProduct = QProduct.product;
            QUser qUser = QUser.user;
            return productRepository.query(query -> query.select(Projections.bean(FindProductRes.class, qProduct, qUser.name.as("createUserName")))
                .from(qProduct)
                .leftJoin(qUser).on(qProduct.createUserId.eq(qUser.id))
                .where(qProduct.companyId.eq(companyId).and(qProduct.deleted.eq(false)))
                .orderBy(qProduct.createTime.desc())
                .offset(paging.getOffset()).limit(paging.getLimit())
            ).all().collectList().map(t -> ResVo.success(t));
        } else {
            QProduct qProduct = QProduct.product;
            QProductMember qPm = QProductMember.productMember;
            QUser qUser = QUser.user;
            return productRepository.query(query -> query.select(Projections.bean(FindProductRes.class, qProduct, qUser.name.as("createUserName")))
                .from(qProduct)
                .innerJoin(qPm).on(qProduct.id.eq(qPm.productId).and(qPm.userId.eq(tokenInfo.getUserId())).and(qProduct.companyId.eq(companyId)))
                .innerJoin(qUser).on(qUser.id.eq(qProduct.createUserId))
                .where(qProduct.deleted.isFalse())
                .orderBy(qProduct.createTime.desc())
                .offset(paging.getOffset()).limit(paging.getLimit())
            ).all().collectList().map(t -> ResVo.success(t));
        }
    }

    @Setter
    @Getter
    public static class CreateProductReq {
        @NotBlank
        @Length(max = 254)
        private String productName;
        private String productDesc;
        private List<Long> userIds;
    }

    @PostMapping("/Companies/{companyId}/Products")
    public Mono<ResVo<?>> createProduct(@PathVariable("companyId") Long companyId, TokenInfo tokenInfo, UserCurrentContext context, @RequestBody @Valid CreateProductReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_PM);

        ProductService.CreateProductCommand command = new ProductService.CreateProductCommand();
        command.setCreateUserId(tokenInfo.getUserId());
        command.setProductName(req.getProductName());
        command.setProductDesc(req.getProductDesc());
        command.setCompanyId(companyId);
        command.setMembers(req.getUserIds());
        return productService.createProduct(command).flatMap(it -> Mono.just(ResVo.success(it)));
    }

}
