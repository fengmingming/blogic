package blogic.productline.testcase.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TestCaseStep {

    @NotBlank
    private String number;
    @NotBlank
    private String step;
    private String expectedResult;

}
