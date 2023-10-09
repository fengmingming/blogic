package blogic.im.domain.repository;

import blogic.im.domain.IMMessage;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface IMMessageRepository extends QuerydslR2dbcRepository<IMMessage, Long> {


    default Flux<IMMessage> findMessagesByFromUserId(@Param("userId") long userId, @Param("lastMsgId") long lastMsgId) {
        return findMessagesByFromUserId(userId, lastMsgId, 100);
    }

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted 
            from im_message where from_user_id = :userId and deleted = 0 and id > :lastMsgId limit :limit
            """)
    public Flux<IMMessage> findMessagesByFromUserId(@Param("userId") long userId, @Param("lastMsgId") long lastMsgId, @Param("limit") long limit);


    default Flux<IMMessage> findMessagesByToUserId(@Param("userId")long userId, @Param("lastMsgId") long lastMsgId) {
        return findMessagesByToUserId(userId, lastMsgId, 100);
    }

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted
            from im_message where to_user_id = :userId and deleted = 0 and id > :lastMsgId limit :limit
            """)
    public Flux<IMMessage> findMessagesByToUserId(@Param("userId")long userId, @Param("lastMsgId") long lastMsgId, @Param("limit") long limit);


    default Flux<IMMessage> findMessagesByGroupId(@Param("groupIds") Collection<Long> groupIds, @Param("lastMsgId") long lastMsgId) {
        return findMessagesByGroupId(groupIds, lastMsgId, 100);
    }

    @Query("""
            select id,from_user_id,to_user_id,group_id,msg_type,content,create_time,update_time,deleted
            from im_message where group_id in :groupIds and deleted = 0 and id > :lastMsgId limit :limit
            """)
    public Flux<IMMessage> findMessagesByGroupId(@Param("groupIds") Collection<Long> groupIds, @Param("lastMsgId") long lastMsgId, @Param("limit") long limit);

}
