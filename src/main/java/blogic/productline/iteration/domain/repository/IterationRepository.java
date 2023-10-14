package blogic.productline.iteration.domain.repository;

import blogic.productline.iteration.domain.Iteration;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IterationRepository extends QuerydslR2dbcRepository<Iteration, Long> {

}
