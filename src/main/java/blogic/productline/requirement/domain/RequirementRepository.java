package blogic.productline.requirement.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends R2dbcRepository<Requirement, Long> {

}
