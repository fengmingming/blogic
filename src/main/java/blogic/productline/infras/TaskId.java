package blogic.productline.infras;

public class TaskId {

    private Long taskId;

    private TaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long get() {
        return this.taskId;
    }

    public TaskId build(Long taskId) {
        return new TaskId(taskId);
    }

}
