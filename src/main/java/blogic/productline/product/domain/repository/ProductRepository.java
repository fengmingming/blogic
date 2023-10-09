package blogic.productline.product.domain.repository;

import blogic.productline.product.domain.Product;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends QuerydslR2dbcRepository<Product, Long> {

    @Query("select user_id from product_member where product_id = :productId")
    public Flux<Long> findMembersByProductId(@Param("productId") long productId);

}
