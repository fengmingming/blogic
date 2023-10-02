package blogic.im.domain;

import blogic.core.domain.BaseEntity;
import blogic.im.*;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("im_message")
public class IMMessage extends BaseEntity {

    @Id
    private Long id;
    @Column("from_user_id")
    private Long fromUserId;
    @Column("to_user_id")
    private Long toUserId;
    @Column("group_id")
    private Long groupId;
    @Column("msg_type")
    private MsgType msgType;
    @Column("content")
    private String content;

    public AbstractContent getContent() {
        JSONObject obj = JSONUtil.parseObj(this.content);
        MsgType msgType = obj.get("msgType", MsgType.class);
        switch (msgType) {
            case TEXT -> {
                return obj.toBean(TextContent.class);
            }
            case IMG -> {
                return obj.toBean(ImageContent.class);
            }
            case VIDEO -> {
                return obj.toBean(VideoContent.class);
            }
            case COMPOSITE -> {
                return obj.toBean(CompositeContent.class);
            }
        }
        throw new IllegalMsgTypeException(msgType);
    }

    public void setContent(AbstractContent content) {
        this.content = JSONUtil.toJsonStr(content);
    }

}
