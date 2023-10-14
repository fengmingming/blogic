package blogic.productline.iteration.domain.repository;

import blogic.productline.iteration.domain.IterationMember;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IterationMemberRepository extends QuerydslR2dbcRepository<IterationMember, Long> {

}
