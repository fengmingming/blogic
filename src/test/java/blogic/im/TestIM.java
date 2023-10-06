package blogic.im;

import blogic.BLogicBootstrap;
import blogic.im.domain.IMMessage;
import blogic.im.domain.repository.IMMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
@Slf4j
public class TestIM {

    @Autowired
    private IMMessageRepository imMessageRepository;

    @Test
    public void testIM() {
        TextContent content = new TextContent();
        content.setContent("xxxxxx");
        content.setMsgType(MsgType.TEXT);
        IMMessage message = new IMMessage();
        message.setFromUserId(1L);
        message.setToUserId(1L);
        message.setMsgType(MsgType.TEXT);
        message.setCreateTime(LocalDateTime.now());
        message.setContent(content);
        imMessageRepository.save(message).block();
    }

    @Test
    public void testIMQuery() {
        System.out.println(imMessageRepository.findMessagesByFromUserId(1L, 4)
                .doOnNext(it -> it.getContent())
                .collectList().block().size());
    }

}
