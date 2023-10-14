package blogic.productline.iteration.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Setter
@Getter
@Table("iteration_member")
public class IterationMember extends ActiveRecord<IterationMember, Long> {

    @Id
    private Long id;
    @Column("iteration_id")
    private Long iterationId;
    @Column("user_id")
    private Long userId;

    @Override
    protected ReactiveCrudRepository<IterationMember, Long> findRepository() {
        return SpringContext.getBean(IterationMemberRepository.class);
    }

    @Override
    protected <S extends IterationMember> S selfS() {
        return (S) this;
    }

}
