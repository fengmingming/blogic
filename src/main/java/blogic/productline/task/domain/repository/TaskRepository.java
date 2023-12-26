package blogic.productline.task.domain.repository;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.iteration.domain.Iteration;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.Task;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface TaskRepository extends QuerydslR2dbcRepository<Task, Long> {

    default Mono<Boolean> verifyTaskBelongToProduct(Long taskId, Long productId) {
        return query(q -> q.select(QTask.task.id.count()).from(QTask.task)
                .where(QTask.task.id.eq(taskId).and(QTask.task.productId.eq(productId))))
                .one().map(it -> it > 0);
    }

    default Mono<Void> verifyTaskBelongToProductOrThrowException(Long taskId, Long productId) {
        return verifyTaskBelongToProduct(taskId, productId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

    default Mono<Map<Long, String>> findByIdsAndToMap(Collection<Long> ids) {
        return findAllById(ids).collectList().map(its -> its.stream().collect(Collectors.toMap(Task::getId, Task::getTaskName)));
    }

}
