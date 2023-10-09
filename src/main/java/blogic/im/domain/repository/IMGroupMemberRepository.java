package blogic.im.domain.repository;

import blogic.im.domain.IMGroupMember;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IMGroupMemberRepository extends QuerydslR2dbcRepository<IMGroupMember, Long> {

    public Flux<IMGroupMember> findByUserIdAndDeleted(Long userId, boolean deleted);
}
