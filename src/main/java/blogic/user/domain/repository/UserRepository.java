package blogic.user.domain.repository;

import blogic.user.domain.QUser;
import blogic.user.domain.User;
import cn.hutool.core.collection.CollectionUtil;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public interface UserRepository extends QuerydslR2dbcRepository<User, Long> {

    /**
     * 手机号查用户
     * */
    public Mono<User> findByPhone(String phone);

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> userIds) {
        if(CollectionUtil.isEmpty(userIds)) {
            return Mono.just(Collections.emptyMap());
        }
        QUser qUser = QUser.user;
        return query(q -> q.select(qUser).from(qUser).where(qUser.id.in(userIds))).all().collectList().map(it -> it.stream().collect(Collectors.toMap(User::getId, User::getName)));
    }

}
