package blogic.productline.product.domain.repository;

import blogic.productline.product.domain.Product;
import blogic.productline.product.domain.QProduct;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends QuerydslR2dbcRepository<Product, Long> {

    default Mono<Boolean> verifyProductBelongToCompany(Long productId, Long companyId) {
        return query(query -> query.select(QProduct.product.id.count())
                .from(QProduct.product)
                .where(QProduct.product.id.eq(productId).and(QProduct.product.companyId.eq(companyId))))
                .one().map(it -> it > 0);
    }

}
