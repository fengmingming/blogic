package blogic.productline.bug.service;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.changerecord.domain.repository.ChangeRecordRepository;
import blogic.core.DateTimeTool;
import blogic.core.context.SpringContext;
import blogic.core.exception.IllegalArgumentException;
import blogic.core.security.TokenInfo;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.domain.BugStatusEnum;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.bug.mapstruct.BugMapStruct;
import blogic.user.domain.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.el.parser.Token;
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChangeRecordRepository changeRecordRepository;

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

    @Setter
    @Getter
    public static class ConfirmBugCommand {
        @NotNull
        private Long bugId;
        @NotNull
        private Long currentUserId;
        @NotNull
        private Integer bugType;
        @NotNull
        private Integer priority;
        private String remark;
    }

    @Transactional
    public Mono<Void> confirm(@NotNull @Valid ConfirmBugCommand command) {
        Mono<Void> confirmMono = bugRepository.findById(command.getBugId()).doOnNext(bug -> {
            bug.confirm(command.getCurrentUserId(), command.getBugType(), command.getPriority());
        }).flatMap(bug -> bugRepository.save(bug)).then();
        Mono<Void> recordMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).flatMap(user -> {
                ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getBugId(),
                        SpringContext.getMessage("record.13.confirmBug"), command.getRemark());
                return changeRecordRepository.save(record).then();
            });
        });
        return confirmMono.then(recordMono);
    }

    @Setter
    @Getter
    public static class AppointBugCommand {
        @NotNull
        private Long bugId;
        @NotNull
        private Long currentUserId;
        private String remark;
    }

    @Transactional
    public Mono<Void> appoint(@Valid AppointBugCommand command) {
        Mono<Void> appointMono = bugRepository.findById(command.getBugId()).doOnNext(bug -> {
            bug.appoint(command.getCurrentUserId());
        }).flatMap(bug -> bugRepository.save(bug)).then();
        Mono<Void> recordMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).flatMap(user -> {
                ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getBugId(),
                        SpringContext.getMessage("record.13.appointBug", user.getName()), command.getRemark());
                return changeRecordRepository.save(record);
            }).then();
        });
        return appointMono.then(recordMono);
    }

    @Setter
    @Getter
    public static class CloseBugCommand {
        @NotNull
        private Long bugId;
        private String remark;
    }

    @Transactional
    public Mono<Void> closeBug(@Valid CloseBugCommand command) {
        Mono<Void> closeMono = bugRepository.findById(command.getBugId()).doOnNext(bug -> {
            bug.closeBug();
        }).flatMap(bug -> bugRepository.save(bug)).then();
        Mono<Void> recordMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getBugId(),
                    SpringContext.getMessage("record.13.closeBug"), command.getRemark());
            return changeRecordRepository.save(record).then();
        });
        return closeMono.then(recordMono);
    }

    @Setter
    @Getter
    public static class FixBugCommand {
        @NotNull
        private Long bugId;
        @NotNull
        private Integer fixSolution;
        @Length(max = 50)
        private String fixVersion;
        @NotNull
        private LocalDateTime fixTime;
        @NotNull
        private Long currentUserId;
        private String remark;
    }

    @Transactional
    public Mono<Void> fixBug(@Valid FixBugCommand command) {
        Mono<Void> fixMono = bugRepository.findById(command.getBugId()).doOnNext(bug -> {
            bug.fix(command.getFixSolution(), command.getFixVersion(), command.getFixTime(), command.getCurrentUserId());
        }).flatMap(bug -> bugRepository.save(bug)).then();
        Mono<Void> recordMono = Mono.deferContextual(cv -> {
            TokenInfo tokenInfo = cv.get(TokenInfo.class);
            return userRepository.findById(command.getCurrentUserId()).flatMap(user -> {
                ChangeRecord record = buildChangeRecord(tokenInfo.getUserId(), command.getBugId(),
                        SpringContext.getMessage("record.13.fixBug", user.getName(),
                                command.getFixSolution(), command.getFixVersion(), command.getFixTime().format(DateTimeTool.LOCAL_BASIC_DATETIME)),
                        command.getRemark());
                return changeRecordRepository.save(record);
            }).then();
        });
        return fixMono.then(recordMono);
    }

    protected ChangeRecord buildChangeRecord(Long operUserId, Long bugId, String desc, String remark) {
        return ChangeRecord.builder().operUserId(operUserId).keyType(KeyTypeEnum.Bug.getCode()).primaryKey(bugId)
                .operDesc(desc).note(remark).createTime(LocalDateTime.now()).build();
    }

}
