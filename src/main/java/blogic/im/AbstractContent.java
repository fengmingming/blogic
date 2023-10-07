package blogic.im;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractContent {

    @NotNull
    private MsgType msgType;
    @NotNull
    private Long fromUserId;
    private Long toUserId;
    private Long groupId;

}
