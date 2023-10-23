package blogic.productline.product.domain.repository;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.product.domain.ProductMember;
import blogic.productline.product.domain.QProductMember;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductMemberRepository extends QuerydslR2dbcRepository<ProductMember, Long> {

    default Mono<Boolean> verifyUserBelongToProduct(Long userId, Long productId) {
        return query(q -> q.select(QProductMember.productMember.id.count())
                .from(QProductMember.productMember)
                .where(QProductMember.productMember.userId.eq(userId).and(QProductMember.productMember.productId.eq(productId)))
        ).one().map(it -> it > 0);
    }

    default Mono<Void> verifyUserBelongToProductOrThrowException(Long userId, Long productId) {
        return verifyUserBelongToProduct(userId, productId).flatMap(it -> {
           if(it) {
               return Mono.empty();
           }else {
               return Mono.error(new ForbiddenAccessException());
           }
        });
    }

}
