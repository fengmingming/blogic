package blogic.core.rest;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paging {
    private int pageNum = 1;
    private int pageSize = 1000;

    public long getLimit() {
        return this.pageSize;
    }

    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

}
