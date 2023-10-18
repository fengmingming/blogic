package blogic.productline.task.service;

import blogic.productline.task.domain.Task;
import blogic.productline.task.domain.repository.TaskRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Setter
    @Getter
    public static class CreateTaskCommand {
    }

    @Transactional
    public Mono<Task> createTask() {
        return Mono.empty();
    }

}
