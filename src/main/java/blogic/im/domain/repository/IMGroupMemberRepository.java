package blogic.im.domain.repository;

import blogic.im.domain.IMGroupMember;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IMGroupMemberRepository extends R2dbcRepository<IMGroupMember, Long> {

    public Flux<IMGroupMember> findByUserIdAndDeleted(Long userId, boolean deleted);
}
