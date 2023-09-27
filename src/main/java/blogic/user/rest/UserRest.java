package blogic.user.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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

    @PutMapping("/Users")
    public Mono<ResVo> updateUser(TokenInfo tokenInfo, @RequestBody @Valid UpdateUserReq req) {
        UserService.UpdateUserCommand command = new UserService.UpdateUserCommand();
        command.setUserId(tokenInfo.getUserId());
        command.setName(req.getName());
        return userService.updateUser(command).map(it -> ResVo.success());
    }

}
