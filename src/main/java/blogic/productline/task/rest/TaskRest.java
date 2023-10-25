package blogic.productline.task.rest;

import blogic.core.exception.ForbiddenAccessException;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import blogic.user.domain.QUser;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.time.DurationMax;
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
    private ProductLineVerifier productLineVerifier;

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
        Mono<Void> verifyProductIdMono = productLineVerifier.verifyProductOrThrowException(companyId, productId);
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
        private Long requirementId;
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
                                     TokenInfo token, UserCurrentContext context, @RequestBody @Valid CreateTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), null);
        return verifyMono.then(Mono.fromSupplier(() -> {
            TaskService.CreateTaskCommand command = new TaskService.CreateTaskCommand();
            command.setProductId(productId);
            command.setIterationId(req.getIterationId());
            command.setRequirementId(req.getRequirementId());
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
    public static class UpdateTaskReq implements DTOLogicConsistencyVerifier {
        @NotNull
        private Long taskId;
        private Long requirementId;
        private Long iterationId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
        @NotNull
        private Long currentUserId;
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        @NotNull
        private TaskStatusEnum status;
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
        @NotNull
        @Min(0)
        private Integer consumeTime;

        @DurationMax
        private LocalDateTime startTime;
        private LocalDateTime completeTime;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException{
            if(status == TaskStatusEnum.InProgress) {
                if(startTime == null) {
                    throw new IllegalArgumentException("startTime is null");
                }
                this.completeTime = null;
            }
            if(status == TaskStatusEnum.Completed) {
                if (startTime == null || completeTime == null) {
                    throw new IllegalArgumentException("startTime or completeTime is null");
                }
            }

        }

    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Tasks/{taskId}")
    public Mono<ResVo<?>> updateTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                     @PathVariable("taskId")Long taskId, TokenInfo token, UserCurrentContext context,
                                     @RequestBody @Valid UpdateTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Boolean> verifyMono = productLineVerifier.verifyTask(companyId, productId, req.getRequirementId(), req.getIterationId(), req.getTaskId());
        return verifyMono.flatMap(it -> {
            if(it) {
                TaskService.UpdateTaskCommand command = new TaskService.UpdateTaskCommand();
                command.setTaskId(req.getTaskId());
                command.setRequirementId(req.getRequirementId());
                command.setIterationId(req.getIterationId());
                command.setTaskName(req.getTaskName());
                command.setTaskDesc(req.getTaskDesc());
                command.setCurrentUserId(req.getCurrentUserId());
                command.setStatus(req.getStatus());
                command.setStartTime(req.getStartTime());
                command.setCompleteTime(req.getCompleteTime());
                if(req.getStatus() == TaskStatusEnum.Completed) {
                    command.setCompleteUserId(token.getUserId());
                    command.setFinalTime(LocalDateTime.now());
                }
                if(req.getStatus() == TaskStatusEnum.Canceled) {
                    command.setFinalTime(LocalDateTime.now());
                }
                command.setPriority(req.getPriority());
                command.setOverallTime(req.getOverallTime());
                command.setConsumeTime(req.getConsumeTime());
                return taskService.updateTask(command).then(Mono.just(ResVo.success()));
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

    @DTOLogicValid
    public static class TestValid implements DTOLogicConsistencyVerifier{

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            throw new IllegalArgumentException("test valid");
        }

    }

    @PutMapping("/testValid")
    public Mono<Void> testValid(@RequestBody @Valid TestValid valid) {
        return Mono.empty();
    }

}
