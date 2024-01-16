package blogic.core.rest.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StringToNumberArrayDeserializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(StrUtil.isNotBlank(value)) {
            String[] values = value.split(",");
            long [] ls = new long[values.length];
            for(int i = 0,j = values.length;i < j;i++) {
                ls[i] = Long.parseLong(values[i]);
            }
            gen.writeArray(ls, 0, ls.length);
        }else {
            gen.writeNull();
        }
    }

}
