package blogic.user.rest;

import blogic.company.domain.Company;
import blogic.company.domain.Department;
import blogic.company.domain.QDepartment;
import blogic.company.domain.repository.CompanyRepository;
import blogic.company.domain.repository.DepartmentRepository;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.ResVo;
import blogic.core.rest.json.StringToArrayDeserializer;
import blogic.core.rest.json.StringToNumberArrayDeserializer;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.iteration.domain.QIterationMember;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.user.domain.*;
import blogic.user.domain.repository.*;
import blogic.user.service.UserCompanyDto;
import blogic.user.service.UserDepartmentDto;
import blogic.user.service.UserService;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class UserRest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;
    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
    @Autowired
    private IterationMemberRepository iterationMemberRepository;
    @Autowired
    private ProductMemberRepository productMemberRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserInvitationRepository userInvitationRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ProductLineVerifier productLineVerifier;
    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Setter
    @Getter
    public static class CreateUserReq {
        @NotBlank
        @Length(max = 11,min = 11)
        private String phone;
        @NotBlank
        @Length(max = 50,min = 1)
        private String name;
        @NotBlank
        @Length(max = 20,min = 6)
        private String password;
        @Length(max = 200)
        private String companyName;
    }

    @PostMapping("/Users")
    public Mono<ResVo> createUser(@RequestBody @Valid CreateUserReq req) {
        User user = new User();
        user.setPhone(req.getPhone());
        user.setName(req.getName());
        user.setPassword(req.getPassword());
        user.setCreateTime(LocalDateTime.now());
        return userService.createUser(user, req.getCompanyName()).map(it -> ResVo.success());
    }

    @Setter
    @Getter
    public static class UpdateUserReq {
        @Length(max = 100)
        private String name;
    }

    @PutMapping("/Users/{userId}")
    public Mono<ResVo> updateUser(@PathVariable("userId")Long userId, TokenInfo tokenInfo, @RequestBody @Valid UpdateUserReq req) {
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new ForbiddenAccessException());
        UserService.UpdateUserCommand command = new UserService.UpdateUserCommand();
        command.setUserId(tokenInfo.getUserId());
        command.setName(req.getName());
        return userService.updateUser(command).map(it -> ResVo.success());
    }

    @Setter
    @Getter
    public static class UpdateCompanyUserReq {
        @Size(min = 1)
        @NotNull
        private List<Long> departments;
        @Size(min = 1)
        @NotNull
        private List<RoleEnum> roles;
    }

    @PutMapping("/Companies/{companyId}/Users/{userId}")
    public Mono<ResVo<?>> updateCompanyUserInfo(@PathVariable("companyId") Long companyId, @PathVariable("userId") Long userId,
                                                @RequestBody UpdateCompanyUserReq req, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        UserService.UpdateCompanyUserCommand command = new UserService.UpdateCompanyUserCommand();
        command.setCompanyId(companyId);
        command.setUserId(userId);
        command.setRoles(req.getRoles());
        command.setDepartmentIds(req.getDepartments());
        Mono<Boolean> validMono = userService.validUserIdAndCompanyId(userId, companyId);
        return validMono.flatMap(it -> {
           if(it) {
               QDepartment qD = QDepartment.department;
               return departmentRepository.query(q -> q.select(qD).from(qD).where(qD.id.in(req.getDepartments()).and(qD.companyId.eq(companyId)))).all().collectList()
                       .flatMap(departments -> {
                          command.setDepartmentIds(departments.stream().map(Department::getId).collect(Collectors.toList()));
                          return userService.updateCompanyUserInfo(command).then(Mono.just(ResVo.success()));
                       });
           }else {
               return Mono.error(new ForbiddenAccessException());
           }
        });
    }

    @Getter
    @Setter
    public static class FindUserReq {
        @NotNull
        private Long companyId;
        private Long productId;
        private Long iterationId;
    }

    @Setter
    @Getter
    public static class FindUserRes {
        private Long id;
        private String phone;
        private String name;
        private List<Department> departments;
        private List<RoleEnum> roles;
        private Boolean admin;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime joinTime;
    }

    @GetMapping("/Users")
    public Mono<ResVo<?>> findUsers(UserCurrentContext context, FindUserReq req) {
        context.equalsCompanyIdOrThrowException(req.getCompanyId());
        Mono<List<Long>> userIdsMono = Mono.just(Collections.emptyList());
        if(req.getIterationId() != null) {
            QIterationMember qIterationMember = QIterationMember.iterationMember;
            userIdsMono = iterationMemberRepository.query(q -> q.select(qIterationMember.userId).from(qIterationMember).where(qIterationMember.iterationId.eq(req.getIterationId()))).all().collectList();
        }else if(req.getProductId() != null) {
            QProductMember qProductMember = QProductMember.productMember;
            userIdsMono = productMemberRepository.query(q -> q.select(qProductMember.userId).from(qProductMember).where(qProductMember.productId.eq(req.getProductId()))).all().collectList();
        }
        QUser qUser = QUser.user;
        QUserCompanyRole qCR = QUserCompanyRole.userCompanyRole;
        Function<List<FindUserRes>, Mono<List<FindUserRes>>> setDepartments = (users) -> {
            Collection<Long> userIds = users.stream().map(it -> it.getId()).collect(Collectors.toSet());
            if(userIds.size() > 0) {
                QUserDepartment qUD = QUserDepartment.userDepartment;
                QDepartment qD = QDepartment.department;
                return userDepartmentRepository.query(q -> q.select(Projections.bean(UserDepartmentDto.class, qUD.userId, qUD.departmentId, qD.departmentName))
                        .from(qUD).innerJoin(qD).on(qUD.departmentId.eq(qD.id).and(qD.companyId.eq(req.getCompanyId()))).where(qUD.userId.in(userIds)))
                        .all().collectList().map(uds -> {
                            Map<Long, List<UserDepartmentDto>> udMap = uds.stream().collect(Collectors.groupingBy(UserDepartmentDto::getUserId));
                            users.forEach(user -> {
                                List<UserDepartmentDto> its = udMap.get(user.getId());
                                if(its != null) {
                                    user.setDepartments(its.stream().map(it -> {
                                        Department d = new Department();
                                        d.setId(it.getDepartmentId());
                                        d.setDepartmentName(it.getDepartmentName());
                                        return d;
                                    }).collect(Collectors.toList()));
                                }
                            });
                            return users;
                        });
            }
            return Mono.just(users);
        };
        Function<List<FindUserRes>, Mono<List<FindUserRes>>> setRoles = (users) -> {
            Collection<Long> userIds = users.stream().map(it -> it.getId()).collect(Collectors.toSet());
            if(userIds.size() > 0) {
                QUserCompanyRole qUCR = QUserCompanyRole.userCompanyRole;
                return userCompanyRoleRepository.query(q -> q.select(qUCR).from(qUCR).where(qUCR.companyId.eq(req.getCompanyId()).and(qUCR.userId.in(userIds))))
                        .all().collectList().map(ucrs -> {
                            Map<Long, List<UserCompanyRole>> map = ucrs.stream().collect(Collectors.groupingBy(UserCompanyRole::getUserId));
                            users.forEach(user -> {
                                List<UserCompanyRole> roles = map.get(user.getId());
                                user.setRoles(roles.stream().map(it -> it.getRole()).collect(Collectors.toList()));
                                user.setAdmin(roles.stream().filter(it -> it.getAdmin()).findAny().isPresent());
                                user.setJoinTime(roles.stream().map(it -> it.getCreateTime()).min((a, b) -> a.compareTo(b)).get());
                            });
                            return users;
                        });
            }
            return Mono.just(users);
        };
        return userIdsMono.flatMap(userIds -> {
            return userRepository.query(q -> {
                Predicate predicate = qUser.deleted.isFalse();
                if(userIds.size() > 0) {
                    predicate = ExpressionUtils.and(predicate, qUser.id.in(userIds));
                }
                return q.distinct().select(Projections.bean(FindUserRes.class, qUser.id, qUser.name, qUser.phone))
                        .from(qUser).innerJoin(qCR).on(qCR.userId.eq(qUser.id).and(qCR.companyId.eq(req.getCompanyId())))
                        .where(predicate);
            }).all().collectList();
        }).flatMap(setDepartments).flatMap(setRoles).map(it -> ResVo.success(it));
    }

    @GetMapping("/Companies/{companyId}/Users/{userId}")
    public Mono<ResVo<?>> getCompanyUserInfo(@PathVariable("companyId")Long companyId, @PathVariable("userId") Long userId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        QUserCompanyRole qUR = QUserCompanyRole.userCompanyRole;
        Mono<List<UserCompanyRole>> ucrsMono = userCompanyRoleRepository.query(q -> q.select(qUR).from(qUR).where(qUR.userId.eq(userId).and(qUR.companyId.eq(companyId)))).all().collectList();
        QUserDepartment qUD = QUserDepartment.userDepartment;
        QDepartment qD = QDepartment.department;
        Mono<List<Department>> departmentsMono = userDepartmentRepository.query(q -> q.select(qD).from(qUD).innerJoin(qD)
                .on(qD.id.eq(qUD.departmentId).and(qD.companyId.eq(companyId)))
                .where(qUD.userId.eq(userId))).all().collectList();
        return Mono.zip(userRepository.findById(userId), ucrsMono, departmentsMono).map(tuple3 -> {
           if(tuple3.getT2().size() == 0) {
               throw new ForbiddenAccessException();
           }
           User user = tuple3.getT1();
           FindUserRes res = new FindUserRes();
           res.setId(user.getId());
           res.setName(user.getName());
           res.setPhone(user.getPhone());
           res.setRoles(tuple3.getT2().stream().map(it -> it.getRole()).collect(Collectors.toList()));
           res.setDepartments(tuple3.getT3());
           res.setJoinTime(tuple3.getT2().stream().map(it -> it.getCreateTime()).min((a, b) -> a.compareTo(b)).get());
           return ResVo.success(res);
        });
    }

    @GetMapping("/Users/{userId}")
    public Mono<ResVo<?>> getUserInfo(@PathVariable("userId")Long userId, TokenInfo tokenInfo) {
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new ForbiddenAccessException());
        return Mono.zip(userRepository.findById(tokenInfo.getUserId()), userService.findUserCompaniesByUserId(tokenInfo.getUserId()).collectList())
            .map(tuple -> {
                User user = tuple.getT1();
                List<UserCompanyDto> companies = tuple.getT2();
                return ResVo.success(MapUtil.builder()
                    .put("userId", user.getId())
                    .put("userName", user.getName())
                    .put("phone", user.getPhone())
                    .put("companies", companies)
                    .build());
            });
    }

    @Setter
    @Getter
    public static class SwitchContextReq {
        @NotNull
        private Long companyId;
    }

    /**
     * 转换用户上下文
     * */
    @PutMapping("/Users/{userId}/switchContext")
    public Mono<ResVo<?>> switchContext(@PathVariable("userId")Long userId, TokenInfo tokenInfo, Locale locale,
                                        @RequestHeader("Authorization") String authorization, @RequestBody @Valid SwitchContextReq req) {
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new ForbiddenAccessException());//自己只能切换自己上下文
        return userService.validUserIdAndCompanyId(userId, req.getCompanyId()).flatMap(it -> {
            if(it) {
                QUserCompany qUC = QUserCompany.userCompany;
                return userService.switchUserCurrentContext(tokenInfo, req.getCompanyId(),
                        JwtTokenUtil.getTokenFromAuthorization(authorization))
                        .then(userCompanyRepository.query(q -> q.select(qUC).from(qUC).where(qUC.userId.eq(tokenInfo.getUserId()).and(qUC.companyId.eq(req.getCompanyId()))))
                                .one().map(uc -> ResVo.success(MapUtil.builder().put("defProductId", uc.getDefProductId()).build())))
                        .defaultIfEmpty(ResVo.success(MapUtil.builder().put("defProductId", null).build()));
            }
            return Mono.just(ResVo.error(403, locale));
        });
    }

    @Setter
    @Getter
    public static class UserInvitationReq {
        @NotBlank
        @Length(max = 11, min = 11)
        private String phone;
        @NotNull
        @Size(min = 1)
        private List<RoleEnum> roles;
        private List<Long> departmentIds;
    }

    @Setter
    @Getter
    public static class FindUserInvitationsReq {
    }

    @Setter
    @Getter
    public static class FindUserInvitationsRes {
        private Long id;
        private Long companyId;
        private String phone;
        private Long userId;
        @JsonSerialize(using = StringToArrayDeserializer.class)
        private String roles;
        @JsonSerialize(using = StringToNumberArrayDeserializer.class)
        private String departments;
        private Integer status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        private Collection<String> departmentNames;
        private String companyName;
    }

    @GetMapping("/Companies/{companyId}/UserInvitations")
    public Mono<ResVo<?>> findUserInvitations(@PathVariable("companyId")Long companyId, UserCurrentContext context, FindUserInvitationsReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        QUserInvitation qUI = QUserInvitation.userInvitation;
        Mono<List<FindUserInvitationsRes>> uisMono = userInvitationRepository.query(q -> q.select(Projections.bean(FindUserInvitationsRes.class, qUI))
                .from(qUI).where(qUI.companyId.eq(companyId)).orderBy(qUI.id.desc())).all().collectList();
        Function<List<FindUserInvitationsRes>, Mono<List<FindUserInvitationsRes>>> setDepartmentNames = (uis) -> {
            Collection<Long> departmentIds = uis.stream().filter(it -> StrUtil.isNotBlank(it.getDepartments())).flatMap(it -> Arrays.stream(it.getDepartments().split(",")))
                    .map(it -> Long.parseLong(it)).collect(Collectors.toSet());
            if(departmentIds.size() > 0) {
                return departmentRepository.findAllById(departmentIds).collectList().map(its -> {
                    if(its.size() > 0) {
                        Map<Long, String> map = its.stream().collect(Collectors.toMap(Department::getId, Department::getDepartmentName, (a,b) -> a));
                        uis.forEach(ui -> {
                            if(StrUtil.isNotBlank(ui.getDepartments())) {
                                ui.setDepartmentNames(Arrays.stream(ui.getDepartments().split(",")).map(id -> Long.parseLong(id)).map(id -> map.get(id)).filter(name -> name != null).collect(Collectors.toSet()));
                            }
                        });
                    }
                    return uis;
                });
            }else {
                return Mono.just(uis);
            }
        };
        return uisMono.flatMap(setDepartmentNames).map(it -> ResVo.success(it));
    }

    @GetMapping("/Users/{userId}/UserInvitations")
    public Mono<ResVo<?>> findUserInvitationsByUserId(@PathVariable("userId") Long userId, TokenInfo tokenInfo) {
        tokenInfo.equalsUserIdOrThrowException(userId);
        QUserInvitation qUI = QUserInvitation.userInvitation;
        Mono<List<FindUserInvitationsRes>> uisMono = userInvitationRepository.query(q -> q.select(Projections.bean(FindUserInvitationsRes.class, qUI))
                .from(qUI).where(qUI.userId.eq(userId)).orderBy(qUI.id.desc())).all().collectList();
        Function<List<FindUserInvitationsRes>, Mono<List<FindUserInvitationsRes>>> setDepartmentNames = (uis) -> {
            Collection<Long> departmentIds = uis.stream().filter(it -> StrUtil.isNotBlank(it.getDepartments())).flatMap(it -> Arrays.stream(it.getDepartments().split(",")))
                    .map(it -> Long.parseLong(it)).collect(Collectors.toSet());
            if(departmentIds.size() > 0) {
                return departmentRepository.findAllById(departmentIds).collectList().map(its -> {
                    if(its.size() > 0) {
                        Map<Long, String> map = its.stream().collect(Collectors.toMap(Department::getId, Department::getDepartmentName, (a,b) -> a));
                        uis.forEach(ui -> {
                            if(StrUtil.isNotBlank(ui.getDepartments())) {
                                ui.setDepartmentNames(Arrays.stream(ui.getDepartments().split(",")).map(id -> Long.parseLong(id)).map(id -> map.get(id)).filter(name -> name != null).collect(Collectors.toSet()));
                            }
                        });
                        return uis;
                    }else {
                        return uis;
                    }
                });
            }else {
                return Mono.just(uis);
            }
        };
        Function<List<FindUserInvitationsRes>, Mono<List<FindUserInvitationsRes>>> setCompanyName = (uis) -> {
            Collection<Long> companyIds = uis.stream().map(it -> it.getCompanyId()).collect(Collectors.toSet());
            return companyRepository.findAllById(companyIds).collectList().map(companies -> {
                Map<Long, String> map = companies.stream().collect(Collectors.toMap(Company::getId, Company::getCompanyName));
                uis.forEach(ui -> {
                    ui.setCompanyName(map.get(ui.getCompanyId()));
                });
                return uis;
            });
        };
        return uisMono.flatMap(setDepartmentNames).flatMap(setCompanyName).map(it -> ResVo.success(it));
    }

    @PostMapping(value = "/Companies/{companyId}/UserInvitations")
    public Mono<ResVo<?>> userInvitation(@PathVariable("companyId") Long companyId, UserCurrentContext context, @Valid @RequestBody UserInvitationReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Boolean> validMono = userInvitationRepository.existUserInvitation(companyId, req.getPhone());
        QUser qUser = QUser.user;
        Mono<Optional<Long>> userIdMono = userRepository.query(q -> q.select(qUser.id).from(qUser).where(qUser.phone.eq(req.getPhone()).and(qUser.deleted.isFalse())))
                .one().map(it -> Optional.of(it)).switchIfEmpty(Mono.just(Optional.empty()));
        return validMono.flatMap(it -> {
            if(it) {
                return Mono.deferContextual(contextView -> Mono.just(ResVo.error(3001, contextView.get(Locale.class), req.getPhone())));
            }
            return userIdMono.flatMap(userIdOpt -> {
                if(userIdOpt.isPresent()) {
                    return userService.validUserIdAndCompanyId(userIdOpt.get(), companyId).flatMap(exist -> {
                        if(exist) {
                            return Mono.deferContextual(contextView -> Mono.just(ResVo.error(3003, contextView.get(Locale.class), req.getPhone())));
                        }else {
                            UserService.UserInvitationCommand command = new UserService.UserInvitationCommand();
                            command.setCompanyId(companyId);
                            command.setPhone(req.getPhone());
                            command.setUserId(userIdOpt.get());
                            command.setRoles(req.getRoles());
                            command.setDepartmentIds(req.getDepartmentIds());
                            return userService.createUserInvitation(command).then(Mono.just(ResVo.success()));
                        }
                    });
                }else {
                    return Mono.deferContextual(contextView -> Mono.just(ResVo.error(3002, contextView.get(Locale.class), req.getPhone())));
                }
            });
        });
    }

    @PutMapping(value = "/Companies/{companyId}/UserInvitations/{userInvitationId}", params = "action=cancel")
    public Mono<ResVo<?>> cancelInvitation(@PathVariable("companyId") Long companyId, @PathVariable("userInvitationId") Long userInvitationId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        return userInvitationRepository.findById(userInvitationId).flatMap(it -> {
            if(!it.getCompanyId().equals(companyId)) {
                return Mono.error(new ForbiddenAccessException());
            }
            if(it.getStatusEnum() != UserInvitationStatusEnum.Inviting) {
                return Mono.deferContextual(cv -> Mono.just(ResVo.error(3004, cv.get(Locale.class))));
            }
            return userService.cancelUserInvitation(userInvitationId).then(Mono.just(ResVo.success()));
        });
    }

    @PutMapping(value = "/Companies/{companyId}/UserInvitations/{userInvitationId}", params = "action=reInvite")
    public Mono<ResVo<?>> reInvite(@PathVariable("companyId") Long companyId, @PathVariable("userInvitationId") Long userInvitationId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        return userInvitationRepository.findById(userInvitationId).flatMap(it -> {
            if(!it.getCompanyId().equals(companyId)) {
                return Mono.error(new ForbiddenAccessException());
            }
            if(!Arrays.asList(UserInvitationStatusEnum.Reject, UserInvitationStatusEnum.Cancel).contains(it.getStatusEnum())) {
                return Mono.deferContextual(cv -> Mono.just(ResVo.error(3005, cv.get(Locale.class), it.getStatusEnum().getCodeDesc())));
            }
            return userService.validUserIdAndCompanyId(it.getUserId(), companyId).flatMap(valid -> {
               if(valid) {
                   return Mono.deferContextual(cv -> Mono.just(ResVo.error(3003, cv.get(Locale.class), it.getPhone())));
               }else {
                   return userService.reInvite(userInvitationId).then(Mono.just(ResVo.success()));
               }
            });
        });
    }

    @PutMapping(value = "/Users/{userId}/UserInvitations/{userInvitationId}", params = "action=accept")
    public Mono<ResVo<?>> acceptUserInvitation(@PathVariable("userId") Long userId, @PathVariable("userInvitationId") Long userInvitationId, TokenInfo tokenInfo) {
        tokenInfo.equalsUserIdOrThrowException(userId);
        Mono<UserInvitation> validMono = userInvitationRepository.findById(userInvitationId);
        return validMono.flatMap(it -> {
            if(it.getStatusEnum() == UserInvitationStatusEnum.Inviting) {
                return userService.acceptUserInvitation(it).then(Mono.just(ResVo.success()));
            }
            return Mono.just(ResVo.success());
        });
    }

    @PutMapping(value = "/Users/{userId}/UserInvitations/{userInvitationId}", params = "action=reject")
    public Mono<ResVo<?>> rejectUserInvitation(@PathVariable("userId") Long userId, @PathVariable("userInvitationId") Long userInvitationId, TokenInfo tokenInfo) {
        tokenInfo.equalsUserIdOrThrowException(userId);
        return userInvitationRepository.findById(userInvitationId).flatMap(it -> {
            if(!it.getUserId().equals(userId)) {
                return Mono.error(new ForbiddenAccessException());
            }
            if(it.getStatusEnum() != UserInvitationStatusEnum.Inviting) {
                return Mono.deferContextual(cv -> Mono.just(ResVo.error(3004, cv.get(Locale.class))));
            }
            return userService.rejectUserInvitation(userInvitationId).then(Mono.just(ResVo.success()));
        });
    }

    @Setter
    @Getter
    public static class UpdateDefProductReq {
        @NotNull
        private Long productId;
    }

    @PutMapping(value = "/Companies/{companyId}/Users/{userId}", params = "action=setDefProduct")
    public Mono<ResVo<?>> updateDefProduct(@PathVariable("companyId") Long companyId, @PathVariable("userId") Long userId,
                                           UserCurrentContext context, TokenInfo tokenInfo, @RequestBody UpdateDefProductReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        return productLineVerifier.verifyProductOrThrowException(companyId, req.getProductId())
                .then(userService.setDefProduct(userId, companyId, req.getProductId()))
                .then(Mono.just(ResVo.success()));
    }

}
