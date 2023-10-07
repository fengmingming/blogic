package blogic.im;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TextContent extends AbstractContent {

    @NotBlank
    private String content;
}
