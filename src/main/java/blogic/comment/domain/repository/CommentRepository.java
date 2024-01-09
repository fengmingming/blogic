package blogic.comment.domain.repository;

import blogic.comment.domain.Comment;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends QuerydslR2dbcRepository<Comment, Long> {
}
