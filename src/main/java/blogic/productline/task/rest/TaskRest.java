package blogic.productline.task.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.Projections;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class TaskRest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProductRepository productRepository;

    @Initialized

    @Setter
    @Getter
    public static class FindTasksReq extends Paging {
        private String taskName;
        private Long createUserId;
        private Long currentUserId;
        private Long completeUserId;
        private TaskStatusEnum taskStatus;
    }

    @Setter
    @Getter
    public static class FindTasksRes {
        private Long id;
        private Long iterationId;
        private String iterationName;
        private Long productId;
        private String taskName;
        private Integer status;
        private Long currentUserId;
        @Column("currentUserName")
        private String currentUserName;
        private Long completeUserId;
        @Column("completeUserName")
        private String completeUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;
        private Integer allTime;
        private Integer consumeTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Tasks")
    public Mono<ResVo<?>> findTasks(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                    UserCurrentContext context, FindTasksReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyProductIdMono = productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId);
        Mono<ResVo<?>> resVoMono = taskRepository.query(q -> {
            QTask qTask = QTask.task;
            Projections.bean(FindTasksRes.class, qTask, null);
            return q;
        }).all().collectList().map(it -> ResVo.success(it));
        return verifyProductIdMono.then(resVoMono);
    }

}
