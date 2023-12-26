package blogic.productline.iteration.domain.repository;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.iteration.domain.Iteration;
import blogic.productline.iteration.domain.QIteration;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface IterationRepository extends QuerydslR2dbcRepository<Iteration, Long> {

    default Mono<Boolean> verifyIterationBelongToProduct(Long iterationId, Long productId) {
        return query(q -> q.select(QIteration.iteration.id.count()).from(QIteration.iteration)
                .where(QIteration.iteration.id.eq(iterationId).and(QIteration.iteration.productId.eq(productId))))
                .one().map(it -> it > 0);
    }

    default Mono<Void> verifyIterationBelongToProductOrThrowException(Long iterationId, Long productId) {
        return verifyIterationBelongToProduct(iterationId, productId).flatMap(it -> {
           if(it) {
               return Mono.empty();
           }else {
               return Mono.error(new ForbiddenAccessException());
           }
        });
    }

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> ids) {
        return findAllById(ids).collectList().map(its -> its.stream().collect(Collectors.toMap(Iteration::getId, Iteration::getName)));
    }

}
