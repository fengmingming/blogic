package blogic.productline.testcase.domain.repository;

import blogic.productline.testcase.domain.TestCaseStep;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TestCaseStepRepository extends QuerydslR2dbcRepository<TestCaseStep, Long> {

    public Flux<TestCaseStep> findAllByTestCaseId(Iterable<Long> its);

    public Flux<TestCaseStep> findAllByTestCaseId(Long testCaseId);

}
