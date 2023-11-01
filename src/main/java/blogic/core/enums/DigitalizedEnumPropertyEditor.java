package blogic.core.enums;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.lang.Nullable;

import java.beans.PropertyEditorSupport;

public class DigitalizedEnumPropertyEditor extends PropertyEditorSupport {

    private Class<? extends Enum> clazz;

    public DigitalizedEnumPropertyEditor(Class<? extends Enum> clazz) {
        if(!IDigitalizedEnum.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz.getName() + " does not implement " + IDigitalizedEnum.class.getName());
        }
        this.clazz = clazz;
    }

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        if(StrUtil.isNotBlank(text)) {
            setValue(IDigitalizedEnum.findByCode(EnumUtil.getEnumMap(this.clazz).values(), Integer.parseInt(text)));
        }
    }

    @Override
    public String getAsText() {
        IDigitalizedEnum value = (IDigitalizedEnum) getValue();
        if(value != null) {
            return String.valueOf(value.getCode());
        }
        return null;
    }

}
