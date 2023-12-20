package blogic.productline.iteration.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.iteration.domain.repository.IterationRequirementRepository;
import cn.hutool.core.collection.CollectionUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Table("iteration")
public class Iteration extends ActiveRecord<Iteration, Long> {

    @Id
    private Long id;
    @Column("product_id")
    @NotNull
    private Long productId;
    @Column("version_code")
    @NotNull
    @Length(max = 50)
    private String versionCode;
    @Column("name")
    @NotNull
    @Length(max = 254)
    private String name;
    @Column("scheduled_start_time")
    private LocalDate scheduledStartTime;
    @Column("scheduled_end_time")
    private LocalDate scheduledEndTime;
    @Column("status")
    @NotNull
    private Integer status;
    @Column("create_user_id")
    @NotNull
    private Long createUserId;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    public void setStatusEnum(IterationStatusEnum statusEnum) {
        if(statusEnum == null) return;
        this.setStatus(statusEnum.getCode());
    }

    public IterationStatusEnum getStatusEnum() {
        Integer status = getStatus();
        if(status == null) return null;
        return IterationStatusEnum.findIterationStatusEnum(status);
    }

    @Override
    protected ReactiveCrudRepository<Iteration, Long> findRepository() {
        return SpringContext.getBean(IterationRepository.class);
    }

    @Override
    protected <S extends Iteration> S selfS() {
        return (S) this;
    }

    public Mono<Void> saveMembers(List<Long> userIds) {
        Flux<IterationMember> memberFlux = findMembers();
        return memberFlux.collectList().flatMap(its -> {
            List<IterationMember> addMembers = CollectionUtil.subtractToList(userIds, its.stream().map(it -> it.getUserId()).collect(Collectors.toList()))
                    .stream().map(it -> {
                        IterationMember member = new IterationMember();
                        member.setIterationId(Iteration.this.id);
                        member.setUserId(it);
                        return member;
                    }).collect(Collectors.toList());
            List<IterationMember> deleteMembers = its.stream().filter(it -> !userIds.contains(it.getUserId())).collect(Collectors.toList());
            IterationMemberRepository repository = SpringContext.getBean(IterationMemberRepository.class);
            return repository.saveAll(addMembers).then(repository.deleteAll(deleteMembers));
        });
    }

    public Mono<Void> saveRequirements(List<Long> requirementIds) {
        Mono<List<IterationRequirement>> requirementsMono = findRequirements().collectList();
        Mono<List<IterationRequirement>> addRequirementsMono = requirementsMono.map(its -> {
            return CollectionUtil.subtractToList(requirementIds, its.stream().map(it -> it.getRequirementId()).collect(Collectors.toList()))
                    .stream().map(it -> {
                        IterationRequirement requirement = new IterationRequirement();
                        requirement.setRequirementId(it);
                        requirement.setIterationId(Iteration.this.id);
                        return requirement;
                    }).collect(Collectors.toList());
        });
        Mono<List<IterationRequirement>> deleteRequirementsMono = requirementsMono.map(its -> {
            return its.stream().filter(it -> !requirementIds.contains(it.getRequirementId())).collect(Collectors.toList());
        });
        IterationRequirementRepository repository = SpringContext.getBean(IterationRequirementRepository.class);
        return repository.deleteAll(deleteRequirementsMono.flatMapIterable(its -> its))
                .then(repository.saveAll(addRequirementsMono.flatMapMany(its -> Flux.fromStream(its.stream()))).collectList())
                .then(Mono.empty());
    }

    public Flux<IterationMember> findMembers() {
        if(this.id == null) return Flux.empty();
        return SpringContext.getBean(IterationMemberRepository.class).findByIterationId(this.id);
    }

    public Flux<IterationRequirement> findRequirements() {
        if(this.id == null) return Flux.empty();
        return SpringContext.getBean(IterationRequirementRepository.class).findByIterationId(this.id);
    }

}
