package blogic.im.domain;

import blogic.im.*;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("im_message")
public class IMMessage {

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
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

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
