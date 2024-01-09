package blogic.comment.domain;

import blogic.comment.domain.repository.CommentRepository;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Setter
@Getter
@Table("comment")
public class Comment extends ActiveRecord<Comment, Long> {

    @Id
    private Long id;

    @Override
    protected ReactiveCrudRepository<Comment, Long> findRepository() {
        return SpringContext.getBean(CommentRepository.class);
    }

    @Override
    protected <S extends Comment> S selfS() {
        return (S) this;
    }

}
