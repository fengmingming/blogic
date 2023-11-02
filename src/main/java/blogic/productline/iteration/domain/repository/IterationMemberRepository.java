package blogic.productline.iteration.domain.repository;

import blogic.productline.iteration.domain.IterationMember;
import blogic.productline.iteration.domain.QIterationMember;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IterationMemberRepository extends QuerydslR2dbcRepository<IterationMember, Long> {

    default Flux<IterationMember> findByIterationId(Long iterationId) {
        return query(q -> q.select(QIterationMember.iterationMember).from(QIterationMember.iterationMember)
                .where(QIterationMember.iterationMember.iterationId.eq(iterationId))).all();
    }

}
