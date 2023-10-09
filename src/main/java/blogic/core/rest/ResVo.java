package blogic.core.rest;

import blogic.core.context.SpringContext;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Locale;

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

    public static <T> ResVo<PagedVo<T>> success(long total, Collection<T> records) {
        PagedVo<T> pagedVo = new PagedVo<>();
        pagedVo.setRecords(records);
        pagedVo.setTotal(total);
        ResVo<PagedVo<T>> resVo = new ResVo<>();
        resVo.setData(pagedVo);
        return resVo;
    }

    public static <T> ResVo<T> success() {
        ResVo<T> resVo = new ResVo<>();
        return resVo;
    }

    public static <T> ResVo<T> error(int code, String codeDesc) {
        ResVo<T> resVo = new ResVo<>();
        resVo.setCode(code);
        resVo.setCodeDesc(codeDesc);
        return resVo;
    }

    public static <T> ResVo<T> error(String codeDesc) {
        ResVo<T> resVo = new ResVo<>();
        resVo.setCode(500);
        resVo.setCodeDesc(codeDesc);
        return resVo;
    }

    public static <T> ResVo<T> error(int code, Locale locale, Object ... args) {
        ResVo<T> resVo = new ResVo<>();
        resVo.setCode(code);
        resVo.setCodeDesc(SpringContext.INSTANCE().getMessage(String.valueOf(code), args, locale));
        return resVo;
    }

}
