package blogic.productline.task.domain;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.core.DateTimeTool;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.core.domain.LogicConsistencyException;
import blogic.core.domain.LogicConsistencyProcessor;
import blogic.core.enums.IDigitalizedEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.user.domain.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Setter
@Getter
@Table("task")
public class Task extends ActiveRecord<Task, Long> implements LogicConsistencyProcessor {

    @Id
    private Long id;
    @Column("requirement_id")
    private Long requirementId;
    @Column("iteration_id")
    private Long iterationId;
    @Column("product_id")
    private Long productId;
    @Column("task_name")
    private String taskName;
    @Column("task_desc")
    private String taskDesc;
    @Column("parent_id")
    private Long parentId;
    @Column("status")
    private Integer status;
    @Column("current_user_id")
    private Long currentUserId;
    @Column("complete_user_id")
    private Long completeUserId;
    @Column("start_time")
    private LocalDateTime startTime;
    @Column("priority")
    private Integer priority;
    @Column("final_time")
    private LocalDateTime finalTime;
    @Column("complete_time")
    private LocalDateTime completeTime;
    @Column("overall_time")
    private Integer overallTime;
    @Column("consume_time")
    private Integer consumeTime;
    @Column("create_user_id")
    private Long createUserId;
    @Column("create_time")
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<Task, Long> findRepository() {
        return SpringContext.getBean(TaskRepository.class);
    }

    @Override
    protected <S extends Task> S selfS() {
        return (S) this;
    }

    public TaskStatusEnum getTaskStatusEnum() {
        return IDigitalizedEnum.findByCode(Arrays.stream(TaskStatusEnum.values()).collect(Collectors.toList()), this.getStatus());
    }

    public void setStatusEnum(TaskStatusEnum taskStatus) {
        if(taskStatus == null) return;
        this.setStatus(taskStatus.getCode());
    }

    @Override
    public void verifyLogicConsistency() throws LogicConsistencyException {
        TaskStatusEnum status = getTaskStatusEnum();
        if(status == TaskStatusEnum.NotStarted) {
            this.startTime = null;
            this.finalTime = null;
            this.completeTime = null;
            this.completeUserId = null;
        }
        if(status == TaskStatusEnum.InProgress) {
            if(startTime == null) {
                throw new LogicConsistencyException("Task.startTime is null");
            }
            this.finalTime = null;
            this.completeTime = null;
            this.completeUserId = null;
        }
        if(status == TaskStatusEnum.Completed) {
            if (startTime == null) {
                throw new LogicConsistencyException("startTime is null");
            }
            if (finalTime == null) {
                throw new LogicConsistencyException("finalTime is null");
            }
            if (completeTime == null) {
                throw new LogicConsistencyException("completeTime is null");
            }
            if (completeUserId == null) {
                throw new LogicConsistencyException("completeUserId is null");
            }
        }
        if(status == TaskStatusEnum.Canceled) {
            if (finalTime == null) {
                throw new LogicConsistencyException("finalTime is null");
            }
            this.completeTime = null;
            this.completeUserId = null;
        }
    }

    public ChangeRecord appointTaskExecutor(User toUser, Integer consumeTime, String remark) {
        this.setCurrentUserId(toUser.getId());
        this.setConsumeTime(consumeTime + getConsumeTime());
        return buildChangeRecord(SpringContext.getMessage("record.12.appointTask", toUser.getName(), consumeTime), remark);
    }

    public ChangeRecord startTask(User toUser, LocalDateTime startTime, Integer overallTime, Integer consumeTime, String remark) {
        Objects.requireNonNull(toUser);
        Objects.requireNonNull(startTime);
        Objects.requireNonNull(overallTime);
        Objects.requireNonNull(consumeTime);
        this.setCurrentUserId(toUser.getId());
        this.setStartTime(startTime);
        this.setOverallTime(overallTime);
        this.setConsumeTime(consumeTime);
        this.setStatus(TaskStatusEnum.InProgress.getCode());
        return buildChangeRecord(SpringContext.getMessage("record.12.startTask", toUser.getName(), startTime.format(DateTimeTool.LOCAL_BASIC_DATETIME), overallTime, consumeTime), remark);
    }

    public ChangeRecord completeTask(User toUser, Integer consumeTime, LocalDateTime completeTime, String remark) {
        Objects.requireNonNull(toUser.getId());
        Objects.requireNonNull(consumeTime);
        Objects.requireNonNull(completeTime);
        this.setCurrentUserId(toUser.getId());
        this.setConsumeTime(this.getConsumeTime() + consumeTime);
        this.setCompleteTime(completeTime);
        this.setStatus(TaskStatusEnum.Completed.getCode());
        return buildChangeRecord(SpringContext.getMessage("record.12.completeTask", toUser.getName(), consumeTime, completeTime.format(DateTimeTool.LOCAL_BASIC_DATETIME)), remark);
    }

    public ChangeRecord cancelTask(String reason) {
        this.setStatus(TaskStatusEnum.Canceled.getCode());
        return buildChangeRecord(SpringContext.getMessage("record.12.cancelTask"), reason);
    }

    public ChangeRecord pauseTask(String reason) {
        this.setStatus(TaskStatusEnum.Pause.getCode());
        return buildChangeRecord(SpringContext.getMessage("record.12.pauseTask"), reason);
    }

    public ChangeRecord resumeTask(String reason) {
        this.setStatus(TaskStatusEnum.InProgress.getCode());
        return buildChangeRecord(SpringContext.getMessage("record.12.resumeTask"), reason);
    }

    public List<ChangeRecord> recordDailyPaper(List<DailyPaper> dailyPapers) {
        int consumeTime = dailyPapers.stream().mapToInt(it -> it.getConsumeTime()).sum();
        this.setConsumeTime(consumeTime + this.getConsumeTime());
        return dailyPapers.stream().sorted((a,b) -> a.getDate().compareTo(b.getDate())).map(it -> {
            return buildChangeRecord(SpringContext.getMessage("record.12.dailyPaper", it.getDate().format(DateTimeTool.LOCAL_BASIC_DATETIME), it.getConsumeTime(), it.getRemainTime()), null);
        }).collect(Collectors.toList());
    }

    protected ChangeRecord buildChangeRecord(String desc, String remark) {
        return ChangeRecord.builder().keyType(KeyTypeEnum.Task.getCode()).primaryKey(getId()).operDesc(desc).note(remark).createTime(LocalDateTime.now()).build();
    }

}
