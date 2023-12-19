package blogic.core.rest.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class StringToArrayDeserializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(StrUtil.isNotBlank(value)) {
            String[] values = value.split(",");
            gen.writeArray(values, 0, values.length);
        }else {
            gen.writeNull();
        }
    }

}
