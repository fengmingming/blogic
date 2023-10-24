package blogic.productline.infras;

public class RequirementId {

    private Long requirementId;

    private RequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Long get() {
        return this.requirementId;
    }

    public RequirementId build(Long requirementId) {
        return new RequirementId(requirementId);
    }

}
