package blogic.productline.task.service;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.changerecord.domain.repository.ChangeRecordRepository;
import blogic.core.DateTimeTool;
import blogic.core.context.SpringContext;
import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.security.TokenInfo;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.task.domain.DailyPaper;
import blogic.productline.task.domain.Task;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.user.domain.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChangeRecordRepository changeRecordRepository;

    @Setter
    @Getter
    public static class CreateTaskCommand {
        private Long requirementId;
        private Long iterationId;
        @NotNull
        private Long productId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
        private Long parentId;
        private Long currentUserId;
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
        @NotNull
        @Min(0)
        private Integer consumeTime;
        private Long createUserId;
    }

    @Transactional
    public Mono<Task> createTask(@NotNull @Valid CreateTaskCommand command) {
        Task task = new Task();
        task.setProductId(command.getProductId());
        task.setIterationId(command.getIterationId());
        task.setRequirementId(command.getRequirementId());
        task.setTaskName(command.getTaskName());
        task.setTaskDesc(command.getTaskDesc());
        task.setParentId(command.getParentId());
        task.setStatusEnum(TaskStatusEnum.NotStarted);
        task.setCurrentUserId(command.getCurrentUserId());
        task.setPriority(command.getPriority());
        task.setOverallTime(command.getOverallTime());
        task.setConsumeTime(command.getConsumeTime());
        task.setCreateUserId(command.getCreateUserId());
        task.setCreateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateTaskCommand implements DTOLogicConsistencyVerifier {
        @NotNull
        private Long taskId;
        private Long requirementId;
        private Long iterationId;
        @NotBlank
        @Length(max = 254)
        private String taskName;
        private String taskDesc;
        private Long currentUserId;
        @NotNull
        @JsonDeserialize(using = DigitalizedEnumDeserializer.class)
        private TaskStatusEnum status;
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
        @Min(0)
        private Integer consumeTime;
        private LocalDateTime startTime;
        private LocalDateTime finalTime;
        private LocalDateTime completeTime;
        private Long completeUserId;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            if (status == TaskStatusEnum.InProgress) {
                if (startTime == null) {
                    throw new IllegalArgumentException("startTime is null");
                }
                if(currentUserId == null) {
                    throw new IllegalArgumentException("currentUserId is null");
                }
            }
            if (status == TaskStatusEnum.Completed) {
                if (startTime == null) {
                    throw new IllegalArgumentException("startTime is null");
                }
                if (finalTime == null) {
                    throw new IllegalArgumentException("finalTime is null");
                }
                if (completeTime == null) {
                    throw new IllegalArgumentException("completeTime is null");
                }
                if (completeUserId == null) {
                    throw new IllegalArgumentException("completeUserId is null");
                }
                if(currentUserId == null) {
                    throw new IllegalArgumentException("currentUserId is null");
                }
            }
            if (status == TaskStatusEnum.Canceled) {
                if (finalTime == null) {
                    throw new IllegalArgumentException("finalTime is null");
                }
            }
        }
    }

    @Transactional
    public Mono<Task> updateTask(@NotNull @Valid UpdateTaskCommand command) {
        return taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.setTaskName(command.getTaskName());
            task.setTaskDesc(command.getTaskDesc());
            task.setStatusEnum(command.getStatus());
            task.setStartTime(command.getStartTime());
            task.setFinalTime(LocalDateTime.now());
            task.setCompleteTime(command.getCompleteTime());
            task.setCompleteUserId(command.getCompleteUserId());
            task.setCurrentUserId(command.getCurrentUserId());
            task.setOverallTime(command.getOverallTime());
            task.setConsumeTime(command.getConsumeTime());
            task.setRequirementId(command.getRequirementId());
            task.setIterationId(command.getIterationId());
            task.setPriority(command.getPriority());
            task.setUpdateTime(LocalDateTime.now());
        }).flatMap(task -> taskRepository.save(task));
    }

    @Setter
    @Getter
    public static class StartTaskCommand {
        @NotNull
        private Long taskId;
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

    @Transactional
    public Mono<Void> startTask(@NotNull @Valid StartTaskCommand command) {
        Mono<Void> startMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.startTask(command.getCurrentUserId(), command.getStartTime(), command.getOverallTime(), command.getConsumeTime());
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).map(user -> {
                return buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                        SpringContext.getMessage("record.12.startTask", user.getName(), command.getStartTime().format(DateTimeTool.LOCAL_BASIC_DATETIME),
                                command.getOverallTime(), command.getConsumeTime()), command.getRemark());
            }).flatMap(record -> changeRecordRepository.save(record)).then();
        });
        return startMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class AppointTaskCommand {
        @NotNull
        private Long taskId;
        @NotNull
        private Long currentUserId;
        @NotNull
        @Min(0)
        private Integer consumeTime;
        private String remark;
    }

    @Transactional
    public Mono<Void> appointTask(@NotNull @Valid AppointTaskCommand command) {
        Mono<Void> appointMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.appointTaskExecutor(command.getCurrentUserId(), command.getConsumeTime());
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).map(user -> {
                return buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                        SpringContext.getMessage("record.12.appointTask", user.getName(), command.getConsumeTime()), command.getRemark());
            }).flatMap(record -> changeRecordRepository.save(record)).then();
        });
        return appointMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class CompleteTaskCommand {
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

    @Transactional
    public Mono<Void> completeTask(@NotNull @Valid CompleteTaskCommand command) {
        Mono<Void> completeMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.completeTask(command.getCurrentUserId(), command.getConsumeTime(), command.getCompleteTime());
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).map(user -> {
                return buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                        SpringContext.getMessage("record.12.completeTask", user.getName(), command.getConsumeTime(),
                                command.getCompleteTime().format(DateTimeTool.LOCAL_BASIC_DATETIME)), command.getRemark());
            }).flatMap(record -> changeRecordRepository.save(record)).then();
        });
        return completeMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class CancelTaskCommand {
        @NotNull
        private Long taskId;
        private String reason;
    }

    @Transactional
    public Mono<Void> cancelTask(@NotNull @Valid CancelTaskCommand command) {
        Mono<Void> cancelMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.cancelTask();
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                    SpringContext.getMessage("record.12.cancelTask"), command.getReason());
            return changeRecordRepository.save(record).then();
        });
        return cancelMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class PauseTaskCommand {
        @NotNull
        private Long taskId;
        private String reason;
    }

    @Transactional
    public Mono<Void> pauseTask(@NotNull @Valid PauseTaskCommand command) {
        Mono<Void> pauseMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.pauseTask();
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                    SpringContext.getMessage("record.12.pauseTask"), command.getReason());
            return changeRecordRepository.save(record).then();
        });
        return pauseMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class ResumeTaskCommand {
        @NotNull
        private Long taskId;
        private String reason;
    }

    @Transactional
    public Mono<Void> resumeTask(@NotNull @Valid ResumeTaskCommand command) {
        Mono<Void> resumeMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.resumeTask();
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(),
                    SpringContext.getMessage("record.12.resumeTask"), command.getReason());
            return changeRecordRepository.save(record).then();
        });
        return resumeMono.then(recordChangeMono);
    }

    @Setter
    @Getter
    public static class TaskDailyPapersCommand {
        @NotNull
        private Long taskId;
        @NotNull
        @Size(min = 1)
        private List<DailyPaper> dailyPapers;
    }

    @Transactional
    public Mono<Void> recordDailyPapers(@NotNull @Valid TaskDailyPapersCommand command) {
        Mono<Void> resumeMono = taskRepository.findById(command.getTaskId()).doOnNext(task -> {
            task.recordDailyPaper(command.getDailyPapers());
        }).flatMap(task -> taskRepository.save(task)).then();
        Mono<Void> recordChangeMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            List<ChangeRecord> records = command.getDailyPapers().stream().sorted((a,b) -> a.getDate().compareTo(b.getDate())).map(it -> {
                return buildChangeRecord(tokenInfo.getUserId(), command.getTaskId(), SpringContext.getMessage("record.12.dailyPaper", it.getDate().format(DateTimeTool.LOCAL_BASIC_DATE), it.getConsumeTime(), it.getRemainTime()), null);
            }).collect(Collectors.toList());
            return changeRecordRepository.saveAll(records).then();
        });
        return resumeMono.then(recordChangeMono);
    }

    protected ChangeRecord buildChangeRecord(Long operUserId, Long taskId, String desc, String remark) {
        return ChangeRecord.builder().operUserId(operUserId).keyType(KeyTypeEnum.Task.getCode()).primaryKey(taskId).operDesc(desc).note(remark).createTime(LocalDateTime.now()).build();
    }

}
