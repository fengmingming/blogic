package blogic.im.rest;

import blogic.core.exception.DefaultCodedException;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.im.Message;
import blogic.im.service.IMService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
public class IMRest {

    @Autowired
    private IMService imService;

    @PostMapping("/im/{userId}/send")
    public Mono<ResVo<Long>> sendMsg(@PathVariable("userId")Long userId, TokenInfo tokenInfo, @Valid @RequestBody Message message) {
        if(!tokenInfo.getUserId().equals(userId) || !message.getContent().getFromUserId().equals(userId)) {
            return Mono.error(new ForbiddenAccessException());
        }
        if(message.getContent().getToUserId() == null && message.getContent().getGroupId() == null) {
            return Mono.error(DefaultCodedException.build(2001));
        }
        return imService.sendMsg(message).map(it -> ResVo.success(it));
    }

    @Data
    public static class MessageGroup {
        private Long id;
        private Boolean group = false;
    }

    @Setter
    @Getter
    public static class MessageDto extends MessageGroup{
        private Collection<Message> messages = Collections.emptyList();
    }

    @GetMapping("/im/{userId}/receive")
    public Mono<ResVo<List<MessageDto>>> receiveMsg(@PathVariable("userId") Long userId, @RequestParam("lastMsgId") Long lastMsgId) {
        return imService.receiveMsg(userId, lastMsgId).collectMultimap(it -> {
            MessageGroup group = new MessageGroup();
            if(it.getContent().getGroupId() != null) {
                group.setGroup(true);
                group.setId(it.getContent().getGroupId());
                return group;
            }else if(userId.equals(it.getContent().getFromUserId())) {
                group.setId(it.getContent().getToUserId());
            }else if(userId.equals(it.getContent().getToUserId())){
                group.setId(it.getContent().getFromUserId());
            }
            return group;
        }).flatMapMany(entry -> Flux.fromStream(entry.entrySet().stream()).map(it -> {
            MessageDto dto = new MessageDto();
            dto.setGroup(it.getKey().getGroup());
            dto.setId(it.getKey().getId());
            dto.setMessages(it.getValue());
            return dto;
        })).collectList().map(it -> ResVo.success(it));
    }

    @Setter
    @Getter
    public static class CreateImGroup {
        private String groupName;
        @NotNull
        @Size(min = 1)
        private List<Long> members;
    }

    @PostMapping("/im/ImGroups")
    public Mono<ResVo<Long>> createImGroup(TokenInfo tokenInfo, @RequestBody @Valid CreateImGroup group) {
        return imService.createGroup(group.getGroupName(), tokenInfo.getUserId(), group.getMembers())
                .map(it -> ResVo.success(it));
    }

    @PutMapping("/im/ImGroups/{groupId}/Members")
    public Mono<ResVo<?>> addMember(@PathVariable("groupId")Long groupId, @RequestBody Collection<Long> members) {
        return imService.addGroupMember(groupId, members).then(Mono.just(ResVo.success()));
    }

}
