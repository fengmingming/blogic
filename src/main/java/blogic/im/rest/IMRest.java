package blogic.im.rest;

import blogic.im.Message;
import blogic.im.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class IMRest {

    @Autowired
    private IMService imService;

    @PostMapping("/im/{userId}/send")
    public Mono<Long> sendMsg(@PathVariable("userId")Long userId, @RequestBody Message message) {

        return imService.sendMsg(message);
    }

    @GetMapping("/im/{userId}/receive")
    public Flux<Message> receiveMsg(@PathVariable("userId") Long userId, @RequestParam(value = "lastMsgId", required = false) Long lastMsgId) {

        return imService.receiveMsg(userId, lastMsgId);
    }

}
