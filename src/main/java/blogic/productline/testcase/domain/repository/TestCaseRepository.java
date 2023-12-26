package blogic.productline.testcase.domain.repository;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.task.domain.Task;
import blogic.productline.testcase.domain.QTestCase;
import blogic.productline.testcase.domain.TestCase;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface TestCaseRepository extends QuerydslR2dbcRepository<TestCase, Long> {

    default Mono<Boolean> verifyTestCaseBelongToProduct(Long testCaseId, Long productId) {
        return query(q -> q.select(QTestCase.testCase.id.count()).from(QTestCase.testCase)
                .where(QTestCase.testCase.id.eq(testCaseId).and(QTestCase.testCase.productId.eq(productId))))
                .one().map(it -> it > 0);
    }

    default Mono<Void> verifyTestCaseBelongToProductOrThrowException(Long testCaseId, Long productId) {
        return verifyTestCaseBelongToProduct(testCaseId, productId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> ids) {
        return findAllById(ids).collectList().map(its -> its.stream().collect(Collectors.toMap(TestCase::getId, TestCase::getTitle)));
    }

}
