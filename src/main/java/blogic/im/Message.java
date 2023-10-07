package blogic.im;

import blogic.im.json.ContentDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message<T extends AbstractContent> {

    private Long msgId;
    @NotNull
    @JsonDeserialize(using = ContentDeserializer.class)
    @Valid
    private T content;

}
