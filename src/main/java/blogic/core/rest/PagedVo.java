package blogic.core.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Setter
@Getter
public class PagedVo<T>{

    private Long total;
    private Collection<T> records;

}
