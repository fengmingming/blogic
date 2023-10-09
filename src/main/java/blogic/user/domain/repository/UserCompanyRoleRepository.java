package blogic.user.domain.repository;

import blogic.user.domain.UserCompanyRole;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserCompanyRoleRepository extends QuerydslR2dbcRepository<UserCompanyRole, Long> {

    Flux<UserCompanyRole> findByUserId(Long userId);

}
