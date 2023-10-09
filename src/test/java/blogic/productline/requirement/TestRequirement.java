package blogic.productline.requirement;

import blogic.BLogicBootstrap;
import blogic.productline.requirement.domain.RequirementRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
public class TestRequirement {

    @Autowired
    private RequirementRepository requirementRepository;

}
