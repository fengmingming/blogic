package blogic.productline.iteration.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.iteration.domain.repository.IterationRequirementRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Setter
@Getter
@Table("iteration_requirement")
public class IterationRequirement extends ActiveRecord<IterationRequirement, Long> {

    @Id
    private Long id;
    @Column("iteration_id")
    private Long iterationId;
    @Column("requirement_id")
    private Long requirementId;

    @Override
    protected ReactiveCrudRepository<IterationRequirement, Long> findRepository() {
        return SpringContext.getBean(IterationRequirementRepository.class);
    }

    @Override
    protected <S extends IterationRequirement> S selfS() {
        return (S) this;
    }

}
