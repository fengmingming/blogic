package blogic.im;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileInfo extends AbstractContent{

    private String suffix;
    private String originName;
    private String url;

}
