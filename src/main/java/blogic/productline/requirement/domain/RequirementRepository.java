package blogic.productline.requirement.domain;

import blogic.core.exception.ForbiddenAccessException;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

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

}
