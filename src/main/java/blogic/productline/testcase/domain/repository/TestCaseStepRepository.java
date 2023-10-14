package blogic.productline.testcase.domain.repository;

import blogic.productline.testcase.domain.TestCaseStep;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseStepRepository extends QuerydslR2dbcRepository<TestCaseStep, Long> {

}
