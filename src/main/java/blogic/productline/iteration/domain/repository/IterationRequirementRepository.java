package blogic.productline.iteration.domain.repository;

import blogic.productline.iteration.domain.IterationRequirement;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IterationRequirementRepository extends QuerydslR2dbcRepository<IterationRequirement, Long> {

}
