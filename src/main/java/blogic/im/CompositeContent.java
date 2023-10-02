package blogic.im;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class CompositeContent extends AbstractContent{

    private List<? extends AbstractContent> contents = Collections.emptyList();

}
