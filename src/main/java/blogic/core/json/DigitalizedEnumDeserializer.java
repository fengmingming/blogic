package blogic.core.json;

import blogic.core.enums.IDigitalizedEnum;
import blogic.core.exception.IllegalEnumValueException;
import cn.hutool.core.util.EnumUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class DigitalizedEnumDeserializer extends JsonDeserializer<Enum<? extends IDigitalizedEnum>> {

    @Override
    public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Object currentValue = p.currentValue();
        if(currentValue == null) {
            throw new JsonParseException(p, "JsonParser.currentValue is null");
        }
        String currentName = p.getCurrentName();
        Field field = ReflectionUtils.findField(currentValue.getClass(), currentName);
        Class clazz = field.getType();
        if(!clazz.isEnum() || !Arrays.stream(clazz.getInterfaces()).filter(it -> it == IDigitalizedEnum.class).findAny().isPresent()) {
            throw new JsonParseException(p, this.getClass().getName() + "only support Enum for implement IDigitalizedEnums");
        }
        Integer code = p.readValueAs(Integer.class);
        if(code == null) {
            return null;
        }
        Optional<Enum> enumOpt = EnumUtil.getEnumMap(clazz).values().stream().filter(it -> code.equals(((IDigitalizedEnum) it).getCode())).findFirst();
        if(!enumOpt.isPresent()) {
            throw new IllegalEnumValueException(clazz, code);
        }
        return enumOpt.get();
    }

}
