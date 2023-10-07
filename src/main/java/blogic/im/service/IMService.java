package blogic.im.service;

import blogic.im.Message;
import blogic.im.domain.IMGroup;
import blogic.im.domain.IMGroupMember;
import blogic.im.domain.IMMessage;
import blogic.im.domain.repository.IMGroupMemberRepository;
import blogic.im.domain.repository.IMGroupRepository;
import blogic.im.domain.repository.IMMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class IMService {

    @Autowired
    private IMMessageRepository messageRepository;
    @Autowired
    private IMGroupRepository groupRepository;
    @Autowired
    private IMGroupMemberRepository groupMemberRepository;

    @Transactional
    public Mono<Long> sendMsg(Message message) {
        IMMessage imMsg = new IMMessage();
        imMsg.setMsgType(message.getContent().getMsgType());
        imMsg.setContent(message.getContent());
        imMsg.setFromUserId(message.getContent().getFromUserId());
        imMsg.setToUserId(message.getContent().getToUserId());
        imMsg.setGroupId(message.getContent().getGroupId());
        imMsg.setCreateTime(LocalDateTime.now());
        return messageRepository.save(imMsg).map(it -> it.getId());
    }

    public Flux<Message> receiveMsg(Long userId, Long lastMsgId) {
        return messageRepository.findMessagesByFromUserId(userId, lastMsgId)
            .concatWith(messageRepository.findMessagesByToUserId(userId, lastMsgId))
            .concatWith(groupMemberRepository.findByUserIdAndDeleted(userId, false)
                .map(it -> it.getGroupId()).collectList().flatMapMany(its -> {
                        if(its.size() > 0) {
                            return messageRepository.findMessagesByGroupId(its, lastMsgId);
                        }else {
                            return Flux.empty();
                        }
                    }))
            .map(it -> {
                Message message = new Message();
                message.setMsgId(it.getId());
                message.setContent(it.getContent());
                return message;
            });
    }

    @Transactional
    public Mono<Long> createGroup(String groupName, Long userId, Collection<Long> members) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(members);
        IMGroup group = new IMGroup();
        group.setGroupName(groupName);
        group.setCreateTime(LocalDateTime.now());
        return groupRepository.save(group).flatMapMany(it -> {
            return groupMemberRepository.saveAll(members.stream().map(member -> {
                IMGroupMember groupMember = new IMGroupMember();
                groupMember.setGroupId(it.getId());
                groupMember.setAdmin(userId.equals(member));
                groupMember.setCreateTime(LocalDateTime.now());
                groupMember.setUserId(member);
                return groupMember;
            }).collect(Collectors.toList()));
        }).collectList().map(it -> it.get(0).getGroupId());
    }

    @Transactional
    public Mono<Void> addGroupMember(Long groupId, Collection<Long> members) {
        return groupMemberRepository.saveAll(members.stream().map(userId -> {
            IMGroupMember groupMember = new IMGroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setAdmin(false);
            groupMember.setCreateTime(LocalDateTime.now());
            groupMember.setUserId(userId);
            return groupMember;
        }).collect(Collectors.toList())).then();
    }

}
