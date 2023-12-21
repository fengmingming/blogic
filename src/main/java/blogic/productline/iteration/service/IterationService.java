package blogic.productline.iteration.service;

import blogic.productline.iteration.domain.Iteration;
import blogic.productline.iteration.domain.IterationStatusEnum;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.iteration.domain.repository.IterationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        private LocalDate scheduledStartTime;
        private LocalDate scheduledEndTime;
        @NotNull
        private Long createUserId;
        private List<Long> userIds;
        private List<Long> requirementIds;
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
                return it.saveMembers(command.getUserIds())
                        .then(it.saveRequirements(command.getRequirementIds()))
                        .then(Mono.just(it.getId()));
        });
    }

    @Setter
    @Getter
    public static class UpdateIterationCommand {
        @NotNull
        private Long iterationId;
        @NotNull
        @Length(max = 50)
        private String versionCode;
        @NotNull
        @Length(max = 254)
        private String name;
        @NotNull
        private IterationStatusEnum status;
        private LocalDate scheduledStartTime;
        private LocalDate scheduledEndTime;
        @NotNull
        @Size(min = 1)
        private List<Long> userIds;
        private List<Long> requirementIds;
    }

    @Transactional
    public Mono<Void> updateIteration(@Valid UpdateIterationCommand command) {
        Mono<Iteration> iterationMono = iterationRepository.findById(command.getIterationId());
        return iterationMono.flatMap(it -> {
            it.setVersionCode(command.getVersionCode());
            it.setName(command.getName());
            it.setScheduledStartTime(command.getScheduledStartTime());
            it.setScheduledEndTime(command.getScheduledEndTime());
            it.setStatusEnum(command.getStatus());
            return it.save().then(it.saveMembers(command.getUserIds())).then(it.saveRequirements(command.getRequirementIds()));
        });
    }

}
