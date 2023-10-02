package blogic.im;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message<T extends AbstractContent> {

    private Long msgId;
    private T content;

}
