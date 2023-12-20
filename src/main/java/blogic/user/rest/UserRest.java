package blogic.user.rest;

import blogic.company.domain.Department;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.ResVo;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.iteration.domain.QIterationMember;
import blogic.productline.iteration.domain.repository.IterationMemberRepository;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.user.domain.QUser;
import blogic.user.domain.QUserCompanyRole;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import blogic.user.domain.repository.UserDepartmentRepository;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserCompanyDto;
import blogic.user.service.UserService;
import cn.hutool.core.map.MapUtil;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    @Getter
    @Setter
    public static class FindUserReq {
        @NotNull
        private Long companyId;
        private Long productId;
        private Long iterationId;
        private Integer pageNum;
        private Integer pageSize;
    }

    @Setter
    @Getter
    public static class FindUserRes {
        private Long id;
        private String phone;
        private String name;
        private List<Department> departments;
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
        return userIdsMono.flatMap(userIds -> {
            return userRepository.query(q -> {
                Predicate predicate = qUser.deleted.isFalse();
                if(userIds.size() > 0) {
                    predicate = ExpressionUtils.and(predicate, qUser.id.in(userIds));
                }
                SQLQuery query = q.select(Projections.bean(FindUserRes.class, qUser.id, qUser.name, qUser.phone))
                        .distinct()
                        .from(qUser).innerJoin(qCR).on(qCR.userId.eq(qUser.id).and(qCR.companyId.eq(req.getCompanyId())))
                        .where(predicate);
                if(req.getPageNum() != null && req.getPageSize() != null) {
                    long offset = (req.getPageNum() - 1) * req.getPageSize();
                    query.offset(offset).limit(req.getPageSize().longValue());
                }
                return query;
            }).all().collectList();
        }).map(it -> ResVo.success(it));
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

    /**
     * 给用户分配角色
     * 管理员角色分配
     * */
    @PutMapping("/Users/{userId}/Roles")
    public Mono<ResVo<?>> assignRoles(@PathVariable("userId")Long userId, UserCurrentContext context, @RequestBody List<RoleEnum> roles) {
        Long companyId = context.getCompanyId();
        return userService.assignRoles(userId, companyId, roles).then(Mono.just(ResVo.success()));
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
