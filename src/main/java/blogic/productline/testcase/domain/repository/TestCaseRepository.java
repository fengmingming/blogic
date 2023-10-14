package blogic.productline.testcase.domain.repository;

import blogic.productline.testcase.domain.TestCase;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends QuerydslR2dbcRepository<TestCase, Long> {

}
