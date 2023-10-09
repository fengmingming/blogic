package blogic.company.domain.repository;

import blogic.company.domain.Department;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepository extends QuerydslR2dbcRepository<Department, Long> {

    @Query("select count(1) from user_department where user_id = :userId and department_id = :departmentId")
    public Mono<Boolean> existUser(@Param("departmentId") long departmentId, @Param("userId") long userId);

    @Modifying
    @Query("insert into user_department (department_id, user_id) values (:departmentId, :userId)")
    public Mono<Void> bindUser(@Param("departmentId") long departmentId, @Param("userId") long userId);
}
