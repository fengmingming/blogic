package blogic.productline.infras;

public class BugId {

    private Long bugId;

    private BugId(Long bugId) {
        this.bugId = bugId;
    }

    public Long get() {
        return this.bugId;
    }

    public BugId build(Long bugId) {
        return new BugId(bugId);
    }

}
