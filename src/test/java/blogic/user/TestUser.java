package blogic.user;

import blogic.BLogicBootstrap;
import blogic.core.context.SpringContext;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TerminalTypeEnum;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
@Slf4j
public class TestUser {

    @Test
    public void testUserRepository() {
        UserRepository userRep = SpringContext.INSTANCE().getBean(UserRepository.class);
        System.out.println(userRep.findAll().doOnNext(it -> System.out.println(JSONUtil.toJsonStr(it))).log(log.getName()).count().block());
    }

    @Test
    public void testJwt() {
        System.out.println(JwtTokenUtil.generateToken(123L, TerminalTypeEnum.MOBILE, "123456".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testActiveRecord() {
        User user = new User();
        user.setCreateTime(LocalDateTime.now());
        user.setPhone(RandomUtil.randomNumbers(11));
        user.setPassword(BCrypt.hashpw("123456"));
        user.insert().doOnNext(it -> it.setUpdateTime(LocalDateTime.now())).flatMap(it -> it.update().then(it.delete())).block();
    }

}
