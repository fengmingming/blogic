package blogic.dict;

import blogic.BLogicBootstrap;
import blogic.dict.domain.Dict;
import blogic.dict.domain.repository.DictRepository;
import blogic.productline.iteration.domain.IterationStatusEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Locale;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
public class TestDict {

    @Autowired
    private DictRepository dictRepository;

    @Test
    public void testDomain() {
        Dict dict = new Dict();
        dict.setDictType(IterationStatusEnum.class.getName());
        dict.setCode(IterationStatusEnum.Completed.getCode());
        dict.setCodeDesc(IterationStatusEnum.Completed.getCodeDesc());
        dict.setCreateTime(LocalDateTime.now());
        dict.setLocale(Locale.getDefault());
        dictRepository.save(dict).block();
    }

}
