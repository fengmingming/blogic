package blogic.productline.task.rest;

import blogic.core.domain.VerifyLogicConsistency;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import blogic.user.domain.QUser;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.*;
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
    @Autowired
    private ProductMemberRepository productMemberRepository;

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
        private LocalDateTime finalTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;
        private Integer allTime;
        private Integer consumeTime;
        private String createUserId;
        @Column("createUserName")
        private String createUserName;
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
            QUser currentQUser = QUser.user;
            QUser completeQUser = QUser.user;
            QUser createQUser = QUser.user;
            QBean<FindTasksRes> qBean = Projections.bean(FindTasksRes.class, qTask, currentQUser.name.as("currentUserName"), completeQUser.name.as("completeUserName"), createQUser.name.as("createUserName"));
            Predicate predicate = qTask.productId.eq(productId).and(qTask.deleted.eq(false));
            if(StrUtil.isNotBlank(req.getTaskName())) {
                predicate = ExpressionUtils.and(predicate, qTask.taskName.like(req.getTaskName()));
            }
            if(req.getTaskStatus() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.status.eq(req.getTaskStatus().getCode()));
            }
            if(req.getCurrentUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.currentUserId.eq(req.getCurrentUserId()));
            }
            if(req.getCompleteUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.completeUserId.eq(req.getCompleteUserId()));
            }
            if(req.getCreateUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.createUserId.eq(req.getCreateUserId()));
            }
            return q.select(qBean)
                    .from(qTask)
                    .leftJoin(currentQUser).on(qTask.currentUserId.eq(currentQUser.id))
                    .leftJoin(completeQUser).on(qTask.completeUserId.eq(completeQUser.id))
                    .leftJoin(createQUser).on(qTask.createUserId.eq(createQUser.id))
                    .where(predicate)
                    .orderBy(qTask.createTime.desc())
                    .offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList().map(it -> ResVo.success(it));
        return verifyProductIdMono.then(resVoMono);
    }

    @Setter
    @Getter
    public static class CreateTaskReq {
        private Long iterationId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
        @Max(4)
        @Min(1)
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
        private Long currentUserId;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/Tasks")
    public Mono<ResVo<?>> createTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                     TokenInfo token, UserCurrentContext context,
                                     @RequestBody @Valid CreateTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId);
        if(req.getCurrentUserId() != null) {
            verifyMono = verifyMono.then(productMemberRepository.verifyUserBelongToProductOrThrowException(req.getCurrentUserId(), productId));
        }
        return verifyMono.then(Mono.fromSupplier(() -> {
            TaskService.CreateTaskCommand command = new TaskService.CreateTaskCommand();
            command.setProductId(productId);
            command.setIterationId(req.getIterationId());
            command.setTaskName(req.getTaskName());
            command.setTaskDesc(req.getTaskDesc());
            command.setPriority(req.getPriority());
            command.setOverallTime(req.getOverallTime());
            command.setCreateUserId(token.getUserId());
            command.setCurrentUserId(req.getCurrentUserId());
            return command;
        })).flatMap(it -> {
           return taskService.createTask(it);
        }).map(it -> ResVo.success());
    }

    @Setter
    @Getter
    public static class UpdateTaskReq implements VerifyLogicConsistency {
        private Long iterationId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
        @NotNull
        private Long currentUserId;
        @NotNull
        private TaskStatusEnum status;
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
        @Min(0)
        private Integer consumeTime;
        private LocalDateTime startTime;
        private LocalDateTime completeTime;
        private Long completeUserId;

        @Override
        public void afterPropertiesSet() {

        }

    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Tasks/{taskId}")
    public Mono<ResVo<?>> updateTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                     @PathVariable("taskId")Long taskId, TokenInfo token, UserCurrentContext context,
                                     @RequestBody @Valid UpdateTaskReq req) {
        req.afterPropertiesSet();
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId);
        if(req.getCurrentUserId() != null) {
            verifyMono = verifyMono.then(productMemberRepository.verifyUserBelongToProductOrThrowException(req.getCurrentUserId(), productId));
        }
        return null;
    }

}
