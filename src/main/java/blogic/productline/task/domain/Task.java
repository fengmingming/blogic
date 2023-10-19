package blogic.productline.task.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.core.enums.IDigitalizedEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Setter
@Getter
@Table("task")
public class Task extends ActiveRecord<Task, Long> {

    @Id
    private Long id;
    @Column("iteration_id")
    private Long iterationId;
    @Column("product_id")
    private Long productId;
    @Column("task_name")
    private String taskName;
    @Column("task_desc")
    private String taskDesc;
    @Column("status")
    private Integer status;
    @Column("current_user_id")
    private Long currentUserId;
    @Column("complete_user_id")
    private Long completeUserId;
    @Column("start_time")
    private LocalDateTime startTime;
    @Column("end_time")
    private LocalDateTime endTime;
    @Column("complete_time")
    private LocalDateTime completeTime;
    @Column("all_time")
    private Integer allTime;
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

}
