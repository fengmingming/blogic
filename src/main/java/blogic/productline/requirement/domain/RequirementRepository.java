package blogic.productline.requirement.domain;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends QuerydslR2dbcRepository<Requirement, Long> {

}
