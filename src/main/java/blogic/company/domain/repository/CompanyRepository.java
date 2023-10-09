package blogic.company.domain.repository;

import blogic.company.domain.Company;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends QuerydslR2dbcRepository<Company, Long> {

}
