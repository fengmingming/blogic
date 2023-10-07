package blogic.im;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileInfo extends AbstractContent{

    private String suffix;
    private String originName;
    @NotBlank
    private String url;

}
