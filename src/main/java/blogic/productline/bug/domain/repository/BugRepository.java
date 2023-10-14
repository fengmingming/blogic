package blogic.productline.bug.domain.repository;

import blogic.productline.bug.domain.Bug;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BugRepository extends QuerydslR2dbcRepository<Bug, Long> {

}
