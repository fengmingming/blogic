package blogic.productline.product.domain.repository;

import blogic.productline.product.domain.ProductMember;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMemberRepository extends QuerydslR2dbcRepository<ProductMember, Long> {
}
