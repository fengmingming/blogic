package blogic.productline.requirement.domain;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.iteration.domain.Iteration;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface RequirementRepository extends QuerydslR2dbcRepository<Requirement, Long> {

    default Mono<Boolean> verifyRequirementBelongToProduct(Long requirementId, Long productId) {
        return query(q -> q.select(QRequirement.requirement.id.count()).from(QRequirement.requirement)
                .where(QRequirement.requirement.id.eq(requirementId).and(QRequirement.requirement.productId.eq(productId))))
                .one().map(it -> it > 0);
    }

    default Mono<Void> verifyRequirementBelongToProductOrThrowException(Long requirementId, Long productId) {
        return verifyRequirementBelongToProduct(requirementId, productId).flatMap(it -> {
           if(it) {
               return Mono.empty();
           }else {
               return Mono.error(new ForbiddenAccessException());
           }
        });
    }

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> ids) {
        return findAllById(ids).collectList().map(its -> its.stream().collect(Collectors.toMap(Requirement::getId, Requirement::getRequirementName)));
    }

}
