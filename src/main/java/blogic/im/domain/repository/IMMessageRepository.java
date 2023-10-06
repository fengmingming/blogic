package blogic.im.domain.repository;

import blogic.im.domain.IMMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface IMMessageRepository extends R2dbcRepository<IMMessage, Long> {

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted 
            from im_message where from_user_id = :userId and deleted = 0 and id > :lastMsgId
            """)
    public Flux<IMMessage> findMessagesByFromUserId(@Param("userId") Long userId, @Param("lastMsgId") long lastMsgId);

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted
            from im_message where to_user_id = :userId and deleted = 0 and id > :lastMsgId
            """)
    public Flux<IMMessage> findMessagesByToUserId(@Param("userId")Long userId, @Param("lastMsgId") long lastMsgId);

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted
            from im_message where group_id in :groupIds and deleted = 0 and id > :lastMsgId
            """)
    public Flux<IMMessage> findMessagesByGroupId(@Param("groupIds") List<Long> groupIds, @Param("lastMsgId") long lastMsgId);

}
