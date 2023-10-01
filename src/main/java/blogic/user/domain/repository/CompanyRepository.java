package blogic.user.domain.repository;

import blogic.company.domain.Company;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends R2dbcRepository<Company, Long> {

}
