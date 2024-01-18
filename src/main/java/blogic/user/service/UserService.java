package blogic.user.service;

import blogic.company.domain.Company;
import blogic.company.domain.QDepartment;
import blogic.company.domain.repository.CompanyRepository;
import blogic.core.MonoTool;
import blogic.core.exception.DataChangedException;
import blogic.core.security.*;
import blogic.user.domain.*;
import blogic.user.domain.repository.*;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Validated
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AuthenticateFilter.JwtKeyProperties jwtKeyProperties;
    @Autowired
    private UserCurrentContextRepository userCurrentContextRepository;
    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
    @Autowired
    private UserInvitationRepository userInvitationRepository;
    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Transactional
    public Mono<User> createUser(@Valid User user, String companyName) {
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        user.setUpdateTime(user.getCreateTime());
        Company company = new Company();
        if(StrUtil.isNotBlank(companyName)) {
            company.setCompanyName(companyName);
        }else {
            company.setCompanyName(user.getName());
        }
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        return Mono.zip(userRepository.save(user), companyRepository.save(company))
                .flatMap(tuple -> {
                    UserCompany uc = new UserCompany();
                    uc.setUserId(tuple.getT1().getId());
                    uc.setCompanyId(tuple.getT2().getId());
                    uc.setDef(true);
                    uc.setCreateTime(LocalDateTime.now());
                    UserCompanyRole ucr = new UserCompanyRole();
                    ucr.setAdmin(true);
                    ucr.setUserId(tuple.getT1().getId());
                    ucr.setRole(RoleEnum.ROLE_MANAGER);
                    ucr.setCompanyId(tuple.getT2().getId());
                    ucr.setCreateTime(LocalDateTime.now());
                    return userCompanyRoleRepository.save(ucr).then(uc.save()).then(Mono.just(tuple.getT1()));
                });
    }

    @Setter
    @Getter
    public static class UpdateUserCommand {
        @NotNull
        private Long userId;
        @Length(max = 100)
        private String name;
    }

    @Transactional
    public Mono<User> updateUser(@Valid UpdateUserCommand com) {
        return userRepository.findById(com.getUserId()).doOnNext(user -> {
            user.setName(com.getName());
        }).flatMap(user -> userRepository.save(user));
    }

    public Mono<String> createToken(Long userId, TerminalTypeEnum terminal) {
        return userRepository.findById(userId).map(user -> JwtTokenUtil.generateToken(user.getId(),
                terminal, jwtKeyProperties.getKey().getBytes(StandardCharsets.UTF_8)));
    }

    public Flux<UserCompanyDto> findUserCompaniesByUserId(Long userId) {
        return userCompanyRoleRepository.findByUserId(userId).collectList().map(ucrs -> {
            return ucrs.stream().collect(Collectors.groupingBy(UserCompanyRole::getCompanyId));
        }).flatMapIterable(group -> group.entrySet()).flatMap(entry -> companyRepository.findById(entry.getKey())
                .filter(it -> !it.getDeleted()).map(company -> {
                    UserCompanyDto userCompanyDto = new UserCompanyDto();
                    userCompanyDto.setCompanyId(company.getId());
                    userCompanyDto.setUserId(userId);
                    userCompanyDto.setCompanyName(company.getCompanyName());
                    userCompanyDto.setAdmin(entry.getValue().stream().filter(it -> it.getAdmin()).findFirst().isPresent());
                    userCompanyDto.setRoles(entry.getValue().stream().map(it -> it.getRole()).collect(Collectors.toList()));
                    return userCompanyDto;
                })).flatMap(ucDto -> {
                    QUserCompany qUC = QUserCompany.userCompany;
                    return userCompanyRepository.query(q -> q.select(qUC).from(qUC).where(qUC.userId.eq(userId).and(qUC.companyId.eq(ucDto.getCompanyId()))))
                            .one().map(it -> {
                                ucDto.setDef(it.getDef());
                                return ucDto;
                            }).defaultIfEmpty(ucDto);
        });
    }

    /**
     * 验证公司id是否属于用户id
     *
     * @param userId
     * @param companyId
     * @return boolean true
     */
    public Mono<Boolean> validUserIdAndCompanyId(Long userId, Long companyId) {
        return userCompanyRoleRepository.findByUserId(userId).map(it -> it.getCompanyId())
                .filter(it -> it.equals(companyId)).count().map(it -> it > 0).switchIfEmpty(Mono.just(false));
    }

    @Transactional
    public Mono<Void> switchUserCurrentContext(TokenInfo tokenInfo, Long companyId, String token) {
        QUserCompany qUC = QUserCompany.userCompany;
        Mono<Void> saveDefCompany = userCompanyRepository.resetAllDef(tokenInfo.getUserId())
                .then(userCompanyRepository.query(q -> q.select(qUC).from(qUC).where(qUC.userId.eq(tokenInfo.getUserId()).and(qUC.companyId.eq(companyId)))).one()
                        .flatMap(uc -> {
                            uc.setDef(true);
                            return userCompanyRepository.save(uc).then();
                        }));
        return Mono.zip(companyRepository.findById(companyId), userCompanyRoleRepository.findByUserId(tokenInfo.getUserId())
                        .filter(it -> it.getCompanyId().equals(companyId)).map(it -> it.getRole()).collectList())
                .flatMap(tuple -> {
                    Company company = tuple.getT1();
                    List<RoleEnum> roles = tuple.getT2();
                    UserCurrentContext context = UserCurrentContext.builder().token(token).companyId(company.getId())
                            .companyName(company.getCompanyName()).authorities(roles).build();
                    return userCurrentContextRepository.save(tokenInfo, context, jwtKeyProperties.getTimeout(), TimeUnit.MINUTES);
                }).then(saveDefCompany);
    }

    @Setter
    @Getter
    public static class UpdateCompanyUserCommand {
        @NotNull
        private Long userId;
        @NotNull
        @Size(min = 1)
        private List<Long> departmentIds;
        @NotNull
        @Size(min = 1)
        private List<RoleEnum> roles;
        @NotNull
        private Long companyId;
    }

    @Transactional
    public Mono<Void> updateCompanyUserInfo(@NotNull @Valid UpdateCompanyUserCommand command) {
        QUserDepartment qUD = QUserDepartment.userDepartment;
        QDepartment qD = QDepartment.department;
        Mono<Void> updateDepartment = userDepartmentRepository.query(q -> q.select(qUD)
                        .from(qUD).innerJoin(qD).on(qD.id.eq(qUD.departmentId).and(qD.companyId.eq(command.getCompanyId())))
                        .where(qUD.userId.eq(command.getUserId()))).all().collectList()
                .flatMap(uds -> {
                    List<Long> existIds = uds.stream().map(it -> it.getDepartmentId()).collect(Collectors.toList());
                    List<Long> addList = CollectionUtil.subtractToList(command.getDepartmentIds(), existIds);
                    List<Long> removeList = CollectionUtil.subtractToList(existIds, command.getDepartmentIds());
                    Mono<Void> mono = Mono.empty();
                    if (addList.size() > 0) {
                        mono = mono.then(userDepartmentRepository.saveAll(addList.stream().map(it -> {
                            UserDepartment userDepartment = new UserDepartment();
                            userDepartment.setDepartmentId(it);
                            userDepartment.setUserId(command.getUserId());
                            return userDepartment;
                        }).collect(Collectors.toList())).collectList()).then();
                    }
                    if (removeList.size() > 0) {
                        mono = mono.then(userDepartmentRepository.deleteWhere(qUD.userId.eq(command.getUserId()).and(qUD.departmentId.in(removeList)))).then();
                    }
                    return mono;
                });
        QUserCompanyRole qUCR = QUserCompanyRole.userCompanyRole;
        Mono<Void> updateRole = userCompanyRoleRepository.query(q -> q.select(qUCR)
                        .from(qUCR)
                        .where(qUCR.userId.eq(command.getUserId()).and(qUCR.companyId.eq(command.getCompanyId())))).all().collectList()
                .flatMap(ucrs -> {
                    boolean admin = ucrs.stream().filter(it -> it.getAdmin()).findAny().isPresent();
                    List<RoleEnum> existRoles = ucrs.stream().map(it -> it.getRole()).collect(Collectors.toList());
                    List<RoleEnum> addList = CollectionUtil.subtractToList(command.getRoles(), existRoles);
                    List<RoleEnum> removeList = CollectionUtil.subtractToList(existRoles, command.getRoles());
                    if(admin) {
                        removeList.remove(RoleEnum.ROLE_MANAGER);
                    }
                    Mono<Void> mono = Mono.empty();
                    if (addList.size() > 0) {
                        mono = mono.then(userCompanyRoleRepository.saveAll(addList.stream().map(it -> {
                            UserCompanyRole ucr = new UserCompanyRole();
                            ucr.setCompanyId(command.getCompanyId());
                            ucr.setUserId(command.getUserId());
                            ucr.setRole(it);
                            ucr.setAdmin(false);
                            ucr.setCreateTime(LocalDateTime.now());
                            return ucr;
                        }).collect(Collectors.toList())).collectList()).then();
                    }
                    if (removeList.size() > 0) {
                        mono = mono.then(userCompanyRoleRepository.deleteWhere(qUCR.companyId.eq(command.getCompanyId())
                                .and(qUCR.userId.eq(command.getUserId()).and(qUCR.role.in(removeList.stream().map(it -> it.name()).collect(Collectors.toSet())))))).then();
                    }
                    return mono;
                });
        return updateDepartment.then(updateRole);
    }

    @Setter
    @Getter
    public static class UserInvitationCommand {
        @NotNull
        private Long companyId;
        @NotNull
        private Long userId;
        @NotBlank
        private String phone;
        @NotNull
        @Size(min = 1)
        private List<RoleEnum> roles;
        private List<Long> departmentIds;
    }

    /**
     * 创建用户邀请
     */
    @Transactional
    public Mono<Void> createUserInvitation(@NotNull @Valid UserInvitationCommand command) {
        UserInvitation invitation = new UserInvitation();
        invitation.setCompanyId(command.getCompanyId());
        invitation.setUserId(command.getUserId());
        invitation.setPhone(command.getPhone());
        invitation.setStatus(UserInvitationStatusEnum.Inviting.getCode());
        invitation.setRoles(command.getRoles().stream().map(it -> it.name()).distinct().collect(Collectors.joining(",")));
        if (CollectionUtil.isNotEmpty(command.getDepartmentIds())) {
            invitation.setDepartments(command.getDepartmentIds().stream().distinct().map(it -> String.valueOf(it)).collect(Collectors.joining(",")));
        }
        invitation.setCreateTime(LocalDateTime.now());
        return userInvitationRepository.save(invitation).then();
    }

    /**
     * 接受用户邀请
     */
    @Transactional
    public Mono<Void> acceptUserInvitation(@NotNull UserInvitation userInvitation) {
        Mono<Boolean> existMono = this.validUserIdAndCompanyId(userInvitation.getUserId(), userInvitation.getCompanyId());
        Mono<Void> userCompanyMono = Mono.defer(() -> {
            List<UserCompanyRole> ucrList = Arrays.stream(userInvitation.getRoles().split(",")).map(it -> {
                UserCompanyRole ucr = new UserCompanyRole();
                ucr.setCompanyId(userInvitation.getCompanyId());
                ucr.setUserId(userInvitation.getUserId());
                ucr.setRole(RoleEnum.valueOf(it));
                ucr.setAdmin(false);
                ucr.setCreateTime(LocalDateTime.now());
                return ucr;
            }).collect(Collectors.toList());
            return userCompanyRoleRepository.saveAll(ucrList).then();
        });
        Mono<Void> userDepartmentMono = Mono.defer(() -> {
            if (StrUtil.isNotBlank(userInvitation.getDepartments())) {
                List<UserDepartment> uds = Arrays.stream(userInvitation.getDepartments().split(",")).map(it -> Long.parseLong(it)).map(it -> {
                    UserDepartment ud = new UserDepartment();
                    ud.setUserId(userInvitation.getUserId());
                    ud.setDepartmentId(it);
                    return ud;
                }).collect(Collectors.toList());
                return userDepartmentRepository.saveAll(uds).then();
            }
            return Mono.empty();
        });
        Mono<Void> updateUserInvitationStatusMono = Mono.defer(() -> {
            QUserInvitation qUI = QUserInvitation.userInvitation;
            return userInvitationRepository.update(u -> u.set(qUI.status, UserInvitationStatusEnum.Agreed.getCode())
                            .where(qUI.id.eq(userInvitation.getId()).and(qUI.status.eq(UserInvitationStatusEnum.Inviting.getCode()))))
                    .flatMap(l -> {
                        if (l > 0) {
                            return Mono.empty();
                        }
                        return Mono.error(new DataChangedException());
                    });
        });
        return existMono.flatMap(it -> {
            if (it) {
                return Mono.empty();
            } else {
                return updateUserInvitationStatusMono.then(userCompanyMono).then(userDepartmentMono);
            }
        });
    }

    @Transactional
    public Mono<Void> cancelUserInvitation(long userInvitationId) {
        QUserInvitation qUI = QUserInvitation.userInvitation;
        return userInvitationRepository.update(u -> u.set(qUI.status, UserInvitationStatusEnum.Cancel.getCode())
                        .where(qUI.id.eq(userInvitationId).and(qUI.status.eq(UserInvitationStatusEnum.Inviting.getCode()))))
                .flatMap(MonoTool.handleUpdateResult());
    }

    @Transactional
    public Mono<Void> rejectUserInvitation(long userInvitationId) {
        QUserInvitation qUI = QUserInvitation.userInvitation;
        return userInvitationRepository.update(u -> u.set(qUI.status, UserInvitationStatusEnum.Reject.getCode())
                        .where(qUI.id.eq(userInvitationId).and(qUI.status.eq(UserInvitationStatusEnum.Inviting.getCode()))))
                .flatMap(MonoTool.handleUpdateResult());
    }

    @Transactional
    public Mono<Void> reInvite(long userInvitationId) {
        QUserInvitation qUI = QUserInvitation.userInvitation;
        return userInvitationRepository.update(u -> u.set(qUI.status, UserInvitationStatusEnum.Inviting.getCode())
                        .where(qUI.id.eq(userInvitationId).and(qUI.status.in(UserInvitationStatusEnum.Reject.getCode(), UserInvitationStatusEnum.Cancel.getCode()))))
                .flatMap(MonoTool.handleUpdateResult());
    }

    @Transactional
    public Mono<Void> setDefProduct(Long userId, Long companyId, Long productId) {
        QUserCompany qUC = QUserCompany.userCompany;
        return userCompanyRepository.update(u -> u.set(qUC.defProductId, productId).where(qUC.userId.eq(userId).and(qUC.companyId.eq(companyId)))).then();
    }

}
