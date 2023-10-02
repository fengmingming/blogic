package blogic.im.domain.repository;

import blogic.im.domain.IMGroup;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMGroupRepository extends R2dbcRepository<IMGroup, Long> {

}
