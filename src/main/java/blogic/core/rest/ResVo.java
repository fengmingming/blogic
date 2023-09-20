package blogic.core.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResVo<T> {

    private int code;
    private String codeDesc;
    private T data;

    public static <T> ResVo<T> success(T data) {
        ResVo<T> resVo = new ResVo<>();
        resVo.setData(data);
        return resVo;
    }

}
