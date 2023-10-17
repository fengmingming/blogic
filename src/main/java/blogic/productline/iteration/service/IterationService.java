package blogic.productline.iteration.service;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.iteration.domain.Iteration;
import blogic.productline.iteration.domain.IterationMember;
import blogic.productline.iteration.domain.IterationStatusEnum;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.iteration.domain.repository.IterationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class IterationService {

    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private IterationMemberRepository iterationMemberRepository;

    @Setter
    @Getter
    public static class CreateIterationCommand {
        @NotNull
        private Long productId;
        @NotNull
        @Length(max = 50)
        private String versionCode;
        @NotNull
        @Length(max = 254)
        private String name;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        @NotNull
        private Long createUserId;
        private List<Long> userIds;
    }

    @Transactional
    public Mono<Long> createIteration(@Valid CreateIterationCommand command) {
        Iteration iteration = new Iteration();
        iteration.setProductId(command.getProductId());
        iteration.setName(command.getName());
        iteration.setStatusEnum(IterationStatusEnum.NotStarted);
        iteration.setVersionCode(command.getVersionCode());
        iteration.setScheduledStartTime(command.getScheduledStartTime());
        iteration.setScheduledEndTime(command.getScheduledEndTime());
        iteration.setCreateUserId(command.getCreateUserId());
        iteration.setCreateTime(LocalDateTime.now());
        return iterationRepository.save(iteration).flatMap(it -> {
            return iterationMemberRepository.saveAll(command.getUserIds().stream().map(userId -> {
                IterationMember member = new IterationMember();
                member.setIterationId(it.getId());
                member.setUserId(userId);
                return member;
            }).collect(Collectors.toList())).then(Mono.just(it.getId()));
        });
    }

}
