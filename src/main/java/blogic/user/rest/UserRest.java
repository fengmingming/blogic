package blogic.user.rest;

import blogic.core.exception.UnauthorizedAccessException;
import blogic.core.rest.ResVo;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserCompanyDto;
import blogic.user.service.UserService;
import cn.hutool.core.map.MapUtil;
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
import java.util.List;
import java.util.Locale;

@RestController
public class UserRest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

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
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new UnauthorizedAccessException());
        UserService.UpdateUserCommand command = new UserService.UpdateUserCommand();
        command.setUserId(tokenInfo.getUserId());
        command.setName(req.getName());
        return userService.updateUser(command).map(it -> ResVo.success());
    }

    @GetMapping("/Users/{userId}")
    public Mono<ResVo<?>> getUserInfo(@PathVariable("userId")Long userId, TokenInfo tokenInfo) {
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new UnauthorizedAccessException());
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
        if(!tokenInfo.getUserId().equals(userId)) return Mono.error(new UnauthorizedAccessException());//自己只能切换自己上下文
        return userService.validUserIdAndCompanyId(userId, req.getCompanyId()).flatMap(it -> {
            if(it) {
                return userService.switchUserCurrentContext(tokenInfo, req.getCompanyId(),
                        JwtTokenUtil.getTokenFromAuthorization(authorization)).then(Mono.just(ResVo.success()));
            }
            return Mono.just(ResVo.error(403, locale));
        });
    }

}
