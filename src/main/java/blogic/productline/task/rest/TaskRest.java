package blogic.productline.task.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TaskRest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;

    @Setter
    @Getter
    public static class FindTasksReq extends Paging {
        private
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Tasks")
    public Mono<ResVo<?>> findTasks(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                    UserCurrentContext context, FindTasksReq req) {

        return Mono.empty();
    }

}
