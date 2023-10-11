package blogic.productline.product.domain.repository;

import blogic.productline.product.domain.Product;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends QuerydslR2dbcRepository<Product, Long> {

}
