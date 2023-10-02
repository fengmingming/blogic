package blogic.im.domain.repository;

import blogic.im.domain.IMMessage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IMMessageRepository extends R2dbcRepository<IMMessage, Long> {

    public Flux<IMMessage> findByFromUserIdAndDeleted(Long fromUserId, boolean deleted);

    public Flux<IMMessage> findByToUserIdAndDeleted(Long toUserId, boolean deleted);

    public Flux<IMMessage> findByGroupIdAndDeleted(Long groupId, boolean deleted);

}
