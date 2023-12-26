package blogic.productline.bug.domain.repository;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.domain.QBug;
import blogic.productline.task.domain.Task;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface BugRepository extends QuerydslR2dbcRepository<Bug, Long> {

    default Mono<Boolean> verifyBugBelongToProduct(Long bugId, Long productId) {
        return query(q -> q.select(QBug.bug.id.count()).from(QBug.bug)
                .where(QBug.bug.id.eq(bugId).and(QBug.bug.productId.eq(productId))))
                .one().map(it -> it > 0);
    }

    default Mono<Void> verifyBugBelongToProductThrowException(Long bugId, Long productId) {
        return verifyBugBelongToProduct(bugId, productId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> ids) {
        return findAllById(ids).collectList().map(its -> its.stream().collect(Collectors.toMap(Bug::getId, Bug::getTitle)));
    }

}
