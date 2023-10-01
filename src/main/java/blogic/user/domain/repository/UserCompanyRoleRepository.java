package blogic.user.domain.repository;

import blogic.user.domain.UserCompanyRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserCompanyRoleRepository extends R2dbcRepository<UserCompanyRole, Long> {

    Flux<UserCompanyRole> findByUserId(Long userId);

}
