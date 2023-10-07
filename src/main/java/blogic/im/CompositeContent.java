package blogic.im;

import blogic.im.json.ContentDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class CompositeContent extends AbstractContent{

    @JsonDeserialize(contentUsing = ContentDeserializer.class)
    @Size(min = 1)
    @Valid
    private List<? extends AbstractContent> contents = Collections.emptyList();

}
