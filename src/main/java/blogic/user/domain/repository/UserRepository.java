package blogic.user.domain.repository;

import blogic.user.domain.User;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends QuerydslR2dbcRepository<User, Long> {

    /**
     * 手机号查用户
     * */
    public Mono<User> findByPhone(String phone);

}
