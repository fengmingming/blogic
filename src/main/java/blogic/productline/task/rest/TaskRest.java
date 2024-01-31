package blogic.productline.task.rest;

import blogic.core.enums.DigitalizedEnumPropertyEditor;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.task.domain.DailyPaper;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class TaskRest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProductLineVerifier productLineVerifier;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private RequirementRepository requirementRepository;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(TaskStatusEnum.class, new DigitalizedEnumPropertyEditor(TaskStatusEnum.class));
    }

    @Setter
    @Getter
    public static class FindTasksReq extends Paging {
        private Long iterationId;
        private Long requirementId;
        private String taskName;
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
        private Long requirementId;
        private String requirementName;
        private Long productId;
        private String taskName;
        private String taskDesc;
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
        private Integer overallTime;
        private Integer consumeTime;
        private Long createUserId;
        @Column("createUserName")
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
        private Integer priority;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Tasks")
    public Mono<ResVo<?>> findTasks(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                    UserCurrentContext context, FindTasksReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyProductIdMono = productLineVerifier.verifyProductOrThrowException(companyId, productId);
        QTask qTask = QTask.task;
        Predicate predicate = qTask.productId.eq(productId).and(qTask.deleted.eq(false));
        if(StrUtil.isNotBlank(req.getTaskName())) {
            predicate = ExpressionUtils.and(predicate, qTask.taskName.like("%" + req.getTaskName() + "%"));
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
        if(req.getIterationId() != null) {
            predicate = ExpressionUtils.and(predicate, qTask.iterationId.eq(req.getIterationId()));
        }
        if(req.getRequirementId() != null) {
            predicate = ExpressionUtils.and(predicate, qTask.requirementId.eq(req.getRequirementId()));
        }
        Predicate predicateFinal = predicate;
        Mono<List<FindTasksRes>> records = taskRepository.query(q -> {
            return q.select(Projections.bean(FindTasksRes.class, qTask))
                    .from(qTask)
                    .where(predicateFinal)
                    .orderBy(qTask.createTime.desc())
                    .offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList();
        Function<List<FindTasksRes>, Mono<List<FindTasksRes>>> setUserIdFunc = (tasks) -> {
            Set<Long> userIdSet = new HashSet<>();
            userIdSet.addAll(tasks.stream().map(it -> it.getCreateUserId()).collect(Collectors.toSet()));
            userIdSet.addAll(tasks.stream().map(it -> it.getCurrentUserId()).filter(Objects::nonNull).collect(Collectors.toSet()));
            userIdSet.addAll(tasks.stream().map(it -> it.getCompleteUserId()).filter(Objects::nonNull).collect(Collectors.toSet()));
            if(userIdSet.size() > 0) {
                return userRepository.findByIdsAndToMap(userIdSet).map(it -> {
                    tasks.stream().forEach(task -> {
                        task.setCreateUserName(it.get(task.getCreateUserId()));
                        task.setCurrentUserName(it.get(task.getCurrentUserId()));
                        task.setCompleteUserName(it.get(task.getCompleteUserId()));
                    });
                    return tasks;
                });
            }else {
                return Mono.just(tasks);
            }
        };
        Function<List<FindTasksRes>, Mono<List<FindTasksRes>>> setIteration = (tasks) -> {
            Set<Long> ids = tasks.stream().map(it -> it.getIterationId()).filter(Objects::nonNull).collect(Collectors.toSet());
            if(ids.size() > 0) {
                return iterationRepository.findByIdsAndToMap(ids).map(map -> {
                    tasks.stream().forEach(task -> {
                        task.setIterationName(map.get(task.getIterationId()));
                    });
                    return tasks;
                });
            }
            return Mono.just(tasks);
        };
        Function<List<FindTasksRes>, Mono<List<FindTasksRes>>> setRequirement = (tasks) -> {
            Set<Long> ids = tasks.stream().map(it -> it.getRequirementId()).filter(Objects::nonNull).collect(Collectors.toSet());
            if(ids.size() > 0) {
                return requirementRepository.findByIdsAndToMap(ids).map(map -> {
                    tasks.stream().forEach(task -> {
                        task.setRequirementName(map.get(task.getRequirementId()));
                    });
                    return tasks;
                });
            }
            return Mono.just(tasks);
        };
        Mono<Long> total = taskRepository.query(q -> {
            return q.select(qTask.id.count())
                    .from(qTask)
                    .where(predicateFinal);
        }).one();
        return verifyProductIdMono.then(Mono.zip(total, records.flatMap(setUserIdFunc).flatMap(setIteration).flatMap(setRequirement))
                .map(it -> ResVo.success(it.getT1(), it.getT2())));
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Tasks/{taskId}")
    public Mono<ResVo<FindTasksRes>> findTasks(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, @PathVariable("taskId")Long taskId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyTaskIdMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        Function<FindTasksRes, Mono<FindTasksRes>> setUserIdMono = (task) -> {
            List<Long> userIds = new ArrayList<>();
            userIds.add(task.getCreateUserId());
            userIds.add(task.getCurrentUserId());
            userIds.add(task.getCompleteUserId());
            userIds = userIds.stream().filter(it -> it != null).collect(Collectors.toList());
            if(userIds.size() > 0) {
                return userRepository.findByIdsAndToMap(userIds).map(map -> {
                    task.setCreateUserName(map.get(task.getCreateUserId()));
                    task.setCurrentUserName(map.get(task.getCurrentUserId()));
                    task.setCompleteUserName(map.get(task.getCompleteUserId()));
                    return task;
                });
            }
            return Mono.just(task);
        };
        Function<FindTasksRes, Mono<FindTasksRes>> setIteration = (task) -> {
            if(task.getIterationId() != null) {
                return iterationRepository.findById(task.getIterationId()).map(iteration -> {
                    task.setIterationName(iteration.getName());
                    return task;
                });
            }
            return Mono.just(task);
        };
        Function<FindTasksRes, Mono<FindTasksRes>> setRequirement = (task) -> {
            if(task.getRequirementId() != null) {
                return requirementRepository.findById(task.getRequirementId()).map(requirement -> {
                    task.setRequirementName(requirement.getRequirementName());
                    return task;
                });
            }
            return Mono.just(task);
        };
        Mono<ResVo<FindTasksRes>> resVoMono = taskRepository.query(q -> {
            QTask qTask = QTask.task;
            return q.select(Projections.bean(FindTasksRes.class, qTask))
                    .from(qTask)
                    .where(qTask.id.eq(taskId));
        }).one().flatMap(setUserIdMono).flatMap(setIteration).flatMap(setRequirement).map(it -> ResVo.success(it));
        return verifyTaskIdMono.then(resVoMono);
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
        private Long parentId;
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
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, req.getRequirementId(), req.getIterationId(), req.getParentId());
        Mono<Void> userVerifyMono = Mono.defer(() -> {
            if(req.getCurrentUserId() != null) {
                return productLineVerifier.containsUserOrThrowException(productId, req.getCurrentUserId());
            }else {
                return Mono.empty();
            }
        });
        return verifyMono.then(userVerifyMono).then(Mono.fromSupplier(() -> {
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
            command.setParentId(req.getParentId());
            return command;
        })).flatMap(it -> {
           return taskService.createTask(it);
        }).map(it -> ResVo.success());
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateTaskReq implements DTOLogicConsistencyVerifier {
        private Long requirementId;
        private Long iterationId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
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
        private LocalDateTime startTime;
        private LocalDateTime completeTime;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException{
            if(status == TaskStatusEnum.InProgress) {
                if(startTime == null) {
                    throw new IllegalArgumentException("startTime is null");
                }
                if(currentUserId == null) {
                    throw new IllegalArgumentException("currentUserId is null");
                }
            }
            if(status == TaskStatusEnum.Completed) {
                if (startTime == null || completeTime == null) {
                    throw new IllegalArgumentException("startTime or completeTime is null");
                }
                if(currentUserId == null) {
                    throw new IllegalArgumentException("currentUserId is null");
                }
            }
            if(status == TaskStatusEnum.Canceled) {
                if (startTime == null) {
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
        Mono<Boolean> verifyMono = productLineVerifier.verifyTask(companyId, productId, req.getRequirementId(), req.getIterationId(), taskId);
        Mono<Boolean> userVerifyMono = Mono.just(true);
        if(req.getCurrentUserId() != null) {
            userVerifyMono = productLineVerifier.containsUser(productId, req.getCurrentUserId());
        }
        return Mono.zip(verifyMono, userVerifyMono).flatMap(tuple2 -> {
            if(tuple2.getT1() && tuple2.getT2()) {
                TaskService.UpdateTaskCommand command = new TaskService.UpdateTaskCommand();
                command.setTaskId(taskId);
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

    @Setter
    @Getter
    public static class StartTaskReq {
        @NotNull
        private Long currentUserId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        @NotNull
        @Min(0)
        private Integer overallTime;
        @NotNull
        @Min(0)
        private Integer consumeTime;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=startTask")
    public Mono<ResVo<?>> startTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                @PathVariable("taskId")Long taskId, @RequestBody @Valid StartTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.StartTaskCommand command = new TaskService.StartTaskCommand();
            command.setTaskId(taskId);
            command.setStartTime(req.getStartTime());
            command.setCurrentUserId(req.getCurrentUserId());
            command.setOverallTime(req.getOverallTime());
            command.setConsumeTime(req.getConsumeTime());
            command.setRemark(req.getRemark());
            return taskService.startTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class AppointTaskReq {
        @NotNull
        private Long currentUserId;
        @NotNull
        @Min(0)
        private Integer consumeTime;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=appointTask")
    public Mono<ResVo<?>> appointTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                   @PathVariable("taskId")Long taskId, @RequestBody @Valid AppointTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.AppointTaskCommand command = new TaskService.AppointTaskCommand();
            command.setTaskId(taskId);
            command.setConsumeTime(req.getConsumeTime());
            command.setCurrentUserId(req.getCurrentUserId());
            command.setRemark(req.getRemark());
            return taskService.appointTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class CompleteTaskReq {
        @NotNull
        private Long taskId;
        @NotNull
        private Long currentUserId;
        @NotNull
        @Min(0)
        private Integer consumeTime;
        @NotNull
        private LocalDateTime completeTime;
        private String remark;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=completeTask")
    public Mono<ResVo<?>> completeTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                       @PathVariable("taskId")Long taskId, @RequestBody @Valid CompleteTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.CompleteTaskCommand command = new TaskService.CompleteTaskCommand();
            command.setTaskId(taskId);
            command.setCompleteTime(req.getCompleteTime());
            command.setConsumeTime(req.getConsumeTime());
            command.setCurrentUserId(req.getCurrentUserId());
            command.setRemark(req.getRemark());
            return taskService.completeTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class CancelTaskReq {
        private String reason;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=cancelTask")
    public Mono<ResVo<?>> cancelTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                       @PathVariable("taskId")Long taskId, @RequestBody @Valid CancelTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.CancelTaskCommand command = new TaskService.CancelTaskCommand();
            command.setTaskId(taskId);
            command.setReason(req.getReason());
            return taskService.cancelTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class PauseTaskReq {
        private String reason;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=pauseTask")
    public Mono<ResVo<?>> pauseTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                     @PathVariable("taskId")Long taskId, @RequestBody @Valid PauseTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.PauseTaskCommand command = new TaskService.PauseTaskCommand();
            command.setTaskId(taskId);
            command.setReason(req.getReason());
            return taskService.pauseTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class ResumeTaskReq {
        private String reason;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=resumeTask")
    public Mono<ResVo<?>> resumeTask(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                    @PathVariable("taskId")Long taskId, @RequestBody @Valid ResumeTaskReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.ResumeTaskCommand command = new TaskService.ResumeTaskCommand();
            command.setTaskId(taskId);
            command.setReason(req.getReason());
            return taskService.resumeTask(command);
        })).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class TaskDailyPapersReq {
        @NotNull
        @Size(min = 1)
        private List<DailyPaper> dailyPapers;
    }

    @PutMapping(value = "/Companies/{companyId}/Products/{productId}/Tasks/{taskId}", params="action=submitDailyPapers")
    public Mono<ResVo<?>> submitDailyPapers(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId, UserCurrentContext context,
                                     @PathVariable("taskId")Long taskId, @RequestBody @Valid TaskDailyPapersReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyMono = productLineVerifier.verifyTaskOrThrowException(companyId, productId, null, null, taskId);
        return verifyMono.then(Mono.defer(() -> {
            TaskService.TaskDailyPapersCommand command = new TaskService.TaskDailyPapersCommand();
            command.setTaskId(taskId);
            command.setDailyPapers(req.getDailyPapers());
            return taskService.recordDailyPapers(command);
        })).then(Mono.just(ResVo.success()));
    }

}
