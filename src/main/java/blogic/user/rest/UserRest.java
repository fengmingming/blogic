package blogic.user.rest;

import blogic.company.domain.Department;
import blogic.company.domain.QDepartment;
import blogic.company.domain.repository.DepartmentRepository;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.ResVo;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.iteration.domain.QIterationMember;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.user.domain.*;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import blogic.user.domain.repository.UserDepartmentRepository;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserCompanyDto;
import blogic.user.service.UserDepartmentDto;
import blogic.user.service.UserService;
import cn.hutool.core.map.MapUtil;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Setter
    @Getter
    public static class CreateUserReq {
        @NotBlank
        private String phone;
        private String name;
        private String password;
    }

    @PostMapping("/Users")
    public Mono<ResVo> createUser(@RequestBody @Valid CreateUserReq req) {
        User user = new User();
        user.setPhone(req.getPhone());
        user.setName(req.getName());
        user.setPassword(req.getPassword());
        user.setCreateTime(LocalDateTime.now());
        return userService.createUser(user).map(it -> ResVo.success());
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
        private List<String> departments;
        private List<RoleEnum> roles;
        private Boolean admin;
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
                                    user.setDepartments(its.stream().map(it -> it.getDepartmentName()).collect(Collectors.toList()));
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
           res.setDepartments(tuple3.getT3().stream().map(it -> it.getDepartmentName()).collect(Collectors.toList()));
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
                return userService.switchUserCurrentContext(tokenInfo, req.getCompanyId(),
                        JwtTokenUtil.getTokenFromAuthorization(authorization)).then(Mono.just(ResVo.success()));
            }
            return Mono.just(ResVo.error(403, locale));
        });
    }

}
