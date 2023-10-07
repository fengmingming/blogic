package blogic.im.json;

import blogic.im.*;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class ContentDeserializer extends JsonDeserializer<AbstractContent> {

    private static final String MSG_TYPE_FIELD = "msgType";

    @Override
    public AbstractContent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();
        if(node.hasNonNull(MSG_TYPE_FIELD)) {
            MsgType msgType = MsgType.valueOf(node.get(MSG_TYPE_FIELD).asText());
            switch (msgType) {
                case TEXT -> {
                    return om.treeToValue(node, TextContent.class);
                }
                case IMG -> {
                    return om.treeToValue(node, ImageContent.class);
                }
                case VIDEO -> {
                    return om.treeToValue(node, VideoContent.class);
                }
                case COMPOSITE -> {
                    return om.treeToValue(node, CompositeContent.class);
                }
                default -> throw new IllegalMsgTypeException(msgType);
            }
        }else {
            throw new JsonParseException(p, "missing field msgType");
        }
    }

}
