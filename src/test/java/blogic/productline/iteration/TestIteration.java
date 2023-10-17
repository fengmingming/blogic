package blogic.productline.iteration;

import blogic.BLogicBootstrap;
import blogic.productline.iteration.service.IterationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
public class TestIteration {

    @Autowired
    private IterationService iterationService;

    @Test
    public void testTransactional() {
        IterationService.CreateIterationCommand command = new IterationService.CreateIterationCommand();
        command.setProductId(1L);
        command.setCreateUserId(1L);
        command.setVersionCode("xxx");
        command.setName("xxx");
        iterationService.createIteration(command).block();
    }

}
