package blogic.core.enums;

import blogic.core.exception.IllegalEnumValueException;
import cn.hutool.core.collection.CollectionUtil;

import java.util.Collection;

public interface IDigitalizedEnum {

    public static <T> T findByCode(Collection<? extends Enum> enums, Integer code) {
        if(code == null) return null;
        if(CollectionUtil.isEmpty(enums)) {
            throw new IllegalArgumentException("IDigitalizedEnum.findByCode arg0.size must great than one");
        }
        for(Enum e : enums) {
            if(e instanceof IDigitalizedEnum de) {
                if(de.getCode() == code) {
                    return (T) e;
                }
            }else {
                throw new IllegalArgumentException("IDigitalizedEnum.findByCode arg0.size must implements IDigitalizedEnum");
            }
        }
        throw new IllegalEnumValueException(enums.stream().findAny().get().getClass(), code);
    }

    int getCode();

    String getCodeDesc();

}
