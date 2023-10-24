package blogic.productline.task;

import blogic.BLogicBootstrap;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.task.service.TaskService;
import jakarta.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
public class TestTask {

    @Autowired
    private TaskService taskService;
    @Autowired
    private Validator validator;

    @DTOLogicValid
    public static class TestDTO implements DTOLogicConsistencyVerifier {

        private Long id;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            throw new IllegalArgumentException("test dto");
        }
    }

    @Test
    public void testDTOLogicValid() {
        TestDTO dto = new TestDTO();
        validator.validate(dto).stream().forEach(v -> System.out.println(v.getMessage()));
    }

}
