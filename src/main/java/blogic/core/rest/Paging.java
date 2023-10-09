package blogic.core.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Setter
@Getter
public class Paging<T> {
    private int pageNum = 1;
    private int pageSize = 100;
}
