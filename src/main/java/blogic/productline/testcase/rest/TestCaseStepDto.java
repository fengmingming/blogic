package blogic.productline.testcase.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestCaseStepDto {

    private Long id;
    private String number;
    private String step;
    private String expectedResult;
    
}
