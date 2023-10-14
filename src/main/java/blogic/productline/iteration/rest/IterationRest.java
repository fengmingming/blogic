package blogic.productline.iteration.rest;

import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.iteration.domain.QIteration;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.iteration.service.IterationService;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.user.domain.QUser;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class IterationRest {

    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private IterationService iterationService;
    @Autowired
    private ProductRepository productRepository;

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

}
