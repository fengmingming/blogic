package blogic.core.domain;

/**
 * 接口 逻辑一致性验证
 * */
public interface LogicConsistencyProcessor {

    public void verifyLogicConsistency() throws LogicConsistencyException;

}
