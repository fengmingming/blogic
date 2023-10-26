package blogic.productline.bug.service;

import blogic.core.exception.IllegalArgumentException;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.domain.BugStatusEnum;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.bug.mapstruct.BugMapStruct;
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
import java.util.function.Function;

@Service
@Validated
public class BugService {

    @Autowired
    private BugRepository bugRepository;
    @Autowired
    private BugMapStruct bugMapStruct;

    @Setter
    @Getter
    public static class CreateBugCommand {
        private Long testCaseId;
        private Long requirementId;
        private Long iterationId;
        @NotNull
        private Long productId;
        private String iterationVersion;
        @NotBlank
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer env;
        @Length(max = 254)
        private String device;
        private String reproSteps;
        @NotNull
        private Integer severity;
        @NotNull
        private Integer priority;
        private Long currentUserId;
        @NotNull
        private Long createUserId;
    }

    @Transactional
    public Mono<Bug> createBug(@NotNull @Valid CreateBugCommand command) {
        Bug bug = bugMapStruct.mapToDomain(command);
        bug.setStatusEnum(BugStatusEnum.UnAssigned);
        bug.setCreateTime(LocalDateTime.now());
        return bugRepository.save(bug);
    }

    @Setter
    @Getter
    @DTOLogicValid
    public static class UpdateBugCommand implements DTOLogicConsistencyVerifier {
        @NotNull
        private Long bugId;
        private Long testCaseId;
        private Long requirementId;
        private Long iterationId;
        @Length(max = 50)
        private String iterationVersion;
        @NotBlank
        @Length(max = 254)
        private String title;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer env;
        @Length(max = 254)
        private String device;
        private String reproSteps;
        @NotNull
        private BugStatusEnum status;
        private Integer severity;
        private Integer priority;
        private Long currentUserId;
        private Long fixUserId;
        private Integer fixSolution;
        @Length(max = 50)
        private String fixVersion;

        @Override
        public void verifyLogicConsistency() throws IllegalArgumentException {
            if(status == BugStatusEnum.Activated || status == BugStatusEnum.Confirmed
                || status == BugStatusEnum.solved) {
                if(currentUserId == null) {
                    throw new IllegalArgumentException("UpdateBugCommand.currentUserId is null");
                }
            }
            if(status == BugStatusEnum.solved) {
                if(fixUserId == null) {
                    throw new IllegalArgumentException("UpdateBugCommand.fixUserId is null");
                }
                if(fixSolution == null) {
                    throw new IllegalArgumentException("UpdateBugCommand.fixSolution is null");
                }
            }
        }
    }

    @Transactional
    public Mono<Bug> updateBug(@NotNull @Valid UpdateBugCommand command) {
        Mono<Bug> bugMono = bugRepository.findById(command.getBugId());
        Function<Bug, Mono<Bug>> saveBug = bug -> {
            bugMapStruct.updateDomain(command, bug);
            return bugRepository.save(bug);
        };
        return bugMono.flatMap(saveBug);
    }

}
