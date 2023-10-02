package blogic.im.service;

import blogic.im.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class IMService {

    public Mono<Long> sendMsg(Message message) {
        return Mono.just(1L);
    }

    public Flux<Message> receiveMsg(Long userId, Long lastMsgId) {
        return Flux.just();
    }

}
