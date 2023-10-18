package blogic.productline.iteration.domain.repository;

import blogic.productline.iteration.domain.IterationRequirement;
import blogic.productline.iteration.domain.QIterationRequirement;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IterationRequirementRepository extends QuerydslR2dbcRepository<IterationRequirement, Long> {

    default Flux<IterationRequirement> findByIterationId(Long iterationId) {
        return query(q -> q.select(QIterationRequirement.iterationRequirement)
                .where(QIterationRequirement.iterationRequirement.iterationId.eq(iterationId)))
                .all();
    }

}
