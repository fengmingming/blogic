package blogic.productline.task.service;

import blogic.core.enums.json.DigitalizedEnumDeserializer;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.task.domain.Task;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Validated
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

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
        private Long currentUserId;
        @NotNull
        private Integer priority;
        @NotNull
        @Min(0)
        private Integer overallTime;
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
        task.setStatusEnum(TaskStatusEnum.NotStarted);
        task.setCurrentUserId(command.getCurrentUserId());
        task.setPriority(command.getPriority());
        task.setOverallTime(command.getOverallTime());
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

}
