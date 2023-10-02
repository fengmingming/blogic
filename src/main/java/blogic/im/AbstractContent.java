package blogic.im;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractContent {

    private MsgType msgType;
    private Long fromUserId;
    private Long toUserId;
    private Long groupId;

}
