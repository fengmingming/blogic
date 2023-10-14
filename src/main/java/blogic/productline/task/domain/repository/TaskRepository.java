package blogic.productline.task.domain.repository;

import blogic.productline.task.domain.Task;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends QuerydslR2dbcRepository<Task, Long> {

}
