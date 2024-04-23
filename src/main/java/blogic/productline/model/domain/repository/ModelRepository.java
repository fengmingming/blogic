package blogic.productline.model.domain.repository;

import blogic.productline.model.domain.Model;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends QuerydslR2dbcRepository<Model, Long> {

}
