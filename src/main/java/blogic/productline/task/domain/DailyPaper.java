package blogic.productline.task.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class DailyPaper {

    @NotNull
    private LocalDate date;
    @NotNull
    @Min(0)
    private Integer consumeTime;
    @NotNull
    @Min(0)
    private Integer remainTime;
    private String remark;

}
