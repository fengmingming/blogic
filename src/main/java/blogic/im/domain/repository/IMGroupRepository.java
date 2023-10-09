package blogic.im.domain.repository;

import blogic.im.domain.IMGroup;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMGroupRepository extends QuerydslR2dbcRepository<IMGroup, Long> {

}
