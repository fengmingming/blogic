package blogic.core.json;

import blogic.core.enums.IDigitalizedEnum;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DigitalizedEnumSerializer extends JsonSerializer<Enum<? extends IDigitalizedEnum>> {

    @Override
    public void serialize(Enum<? extends IDigitalizedEnum> value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        if(value instanceof IDigitalizedEnum eValue) {
            gen.writeNumber(eValue.getCode());
        }else {
            throw new JsonGenerationException("DigitalizedEnumSerializer only support Enum that it implements IDigitalizedEnum", gen);
        }
    }

}
