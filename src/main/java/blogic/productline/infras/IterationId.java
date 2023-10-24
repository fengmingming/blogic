package blogic.productline.infras;

public class IterationId {

    private Long iterationId;

    private IterationId(Long iterationId) {
        this.iterationId = iterationId;
    }

    public Long get() {
        return this.iterationId;
    }

    public IterationId build(Long iterationId) {
        return new IterationId(iterationId);
    }

}
