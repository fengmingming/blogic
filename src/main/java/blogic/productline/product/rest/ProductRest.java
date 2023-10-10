package blogic.productline.product.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.product.service.ProductService;
import blogic.user.domain.QUser;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.Projections;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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
    public Mono<ResVo<?>> findProducts(@PathVariable("companyId")Long companyId, TokenInfo tokenInfo,
                                       UserCurrentContext context, @RequestBody Paging paging) {
        context.equalsCompanyIdOrThrowException(companyId);
        if(context.authenticate(RoleEnum.ROLE_MANAGER)) {
            QProduct qProduct = QProduct.product;
            QUser qUser = QUser.user;
            return productRepository.query(query -> query.select(Projections.bean(FindProductRes.class, qProduct, qUser.name.as("createUserName")))
                .from(qProduct)
                .leftJoin(qUser).on(qProduct.createUserId.eq(qUser.id))
                .where(qProduct.companyId.eq(companyId).and(qProduct.deleted.eq(false)))
                .offset(paging.getOffset()).limit(paging.getLimit())).all().collectList()
                .map(t -> ResVo.success(t));
        }else {

        }
        return Mono.just(ResVo.success());
    }

}
