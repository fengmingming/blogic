package blogic.user;

import blogic.BLogicBootstrap;
import blogic.core.context.SpringContext;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
@Slf4j
public class TestUser {

    @Test
    public void testUserRepository() {
        UserRepository userRep = SpringContext.INSTANCE().getBean(UserRepository.class);
        System.out.println(userRep.findAll().doOnNext(it -> System.out.println(JSONUtil.toJsonStr(it))).log(log.getName()).count().block());
    }

}
