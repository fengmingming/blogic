package blogic.productline.requirement.service;

import blogic.productline.requirement.domain.Requirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.requirement.domain.RequirementStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@Service
@Validated
public class RequirementService {

    @Autowired
    private RequirementRepository requirementRepository;

    @Setter
    @Getter
    public static class CreateRequirementCommand {
        @NotNull
        private Long productId;
        @NotBlank
        @Length(max = 254)
        private String requirementName;
        @Length(max = 254)
        private String requirementSources;
        private String requirementDesc;
        @NotNull
        private Long createUserId;
    }

    @Transactional
    public Mono<Requirement> createRequirement(@NotNull @Valid CreateRequirementCommand command) {
        Requirement r = new Requirement();
        r.setProductId(command.getProductId());
        r.setRequirementName(command.getRequirementName());
        r.setRequirementSources(command.getRequirementSources());
        r.setRequirementDesc(command.getRequirementDesc());
        r.setRequirementStatusEnum(RequirementStatus.Confirmed);
        r.setCreateUserId(command.getCreateUserId());
        r.setCreateTime(LocalDateTime.now());
        return requirementRepository.save(r);
    }

    @Setter
    @Getter
    public static class UpdateRequirementCommand {
        @NotNull
        private Long requirementId;
        @NotBlank
        @Length(max = 254)
        private String requirementName;
        @Length(max = 254)
        private String requirementSources;
        private String requirementDesc;
        @NotNull
        private RequirementStatus requirementStatus;
    }

    @Transactional
    public Mono<Void> updateRequirement(@NotNull @Valid UpdateRequirementCommand command) {
        return requirementRepository.findById(command.getRequirementId())
                .doOnNext(it -> {
                    it.setRequirementName(command.getRequirementName());
                    it.setRequirementSources(command.getRequirementSources());
                    it.setRequirementDesc(command.getRequirementDesc());
                    it.setRequirementStatusEnum(command.getRequirementStatus());
                }).flatMap(it -> {
                    return requirementRepository.save(it);
                }).then();
    }

    @Transactional
    public Mono<Void> deleteRequirement(Long requirementId) {
        return requirementRepository.findById(requirementId)
                .doOnNext(it -> it.setDeleted(true))
                .flatMap(it -> requirementRepository.save(it))
                .then();
    }

}
