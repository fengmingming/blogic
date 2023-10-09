package blogic.core.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Paging {
    private int pageNum = 1;
    private int pageSize = 100;

    public long getLimit() {
        return this.pageSize;
    }

    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

}
