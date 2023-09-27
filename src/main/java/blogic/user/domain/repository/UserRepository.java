package blogic.user.domain.repository;

import blogic.user.domain.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    /**
     * 手机号查用户
     * */
    public Mono<User> findByPhone(String phone);

}
