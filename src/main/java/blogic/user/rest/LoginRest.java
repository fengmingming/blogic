package blogic.user.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.*;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserService;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RestController
public class LoginRest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCurrentContextRepository userCurrentContextRepository;
    @Autowired
    private AuthenticateFilter.JwtKeyProperties jwtKeyProperties;

    @Setter
    @Getter
    public static class LoginReq {
        @NotBlank
        @Length(min = 11, max = 11)
        private String phone;
        @NotBlank
        @Length(max = 20)
        private String password;
        @NotNull
        private TerminalTypeEnum terminal;
    }

    @PostMapping("/login")
    public Mono<ResVo<?>> login(@Valid @RequestBody LoginReq req, Locale locale) {
        return userRepository.findByPhone(req.getPhone()).filter(it -> !it.getDeleted()).flatMap(user -> {
            if(BCrypt.checkpw(req.getPassword(), user.getPassword())) {
                Mono<String> createTokenMono = userService.createToken(user.getId(), req.getTerminal());
                TokenInfo tokenInfo = TokenInfo.builder().userId(user.getId()).terminal(req.getTerminal()).build();
                return userCurrentContextRepository.findAndRefreshIdleTime(tokenInfo)
                        .flatMap(context -> userCurrentContextRepository.delete(tokenInfo))
                        .then(createTokenMono).flatMap(token -> {
                            UserCurrentContext context = UserCurrentContext.builder().build();
                            return userCurrentContextRepository.save(tokenInfo, context, jwtKeyProperties.getTimeout(), TimeUnit.MINUTES)
                                    .then(Mono.just(ResVo.success(MapUtil.builder()
                                            .put("token", token).put("userId",user.getId())
                                            .build())));
                        });
            }else {
                return Mono.just(ResVo.error(1001, locale));
            }
        }).switchIfEmpty(Mono.just(ResVo.error(1002, locale)));
    }

}
