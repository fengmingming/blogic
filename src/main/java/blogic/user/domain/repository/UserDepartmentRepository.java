package blogic.user.domain.repository;

import blogic.user.domain.UserDepartment;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDepartmentRepository extends QuerydslR2dbcRepository<UserDepartment, Long> {

}
