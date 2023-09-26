package blogic.core.rest;

public interface CodedException {

    /**
     * 业务编码
     * */
    public int getCode();

    /**
     * 异常信息模板参数
     * */
    default Object[] getTemplateArgs() {
        return new Object[]{};
    }

}
