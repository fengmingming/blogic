package blogic.productline.task.rest;

import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskRest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;



}
