package blogic.productline.infras;

public class TestCaseId {

    private Long testCaseId;

    private TestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Long get() {
        return this.testCaseId;
    }

    public TestCaseId build(Long testCaseId) {
        return new TestCaseId(testCaseId);
    }

}
