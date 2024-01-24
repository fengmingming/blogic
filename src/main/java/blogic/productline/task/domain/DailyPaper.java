package blogic.productline.task.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class DailyPaper {

    private LocalDate date;
    private Integer consumeTime;
    private Integer remainTime;
    private String remark;

}
