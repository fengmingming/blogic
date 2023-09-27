package blogic.user.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.AuthenticateFilter;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TerminalTypeEnum;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
public class LoginRest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageSource messageSource;
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
        private TerminalTypeEnum terminal;
    }

    @PostMapping("/login")
    public Mono<ResVo<?>> login(Locale locale, @RequestBody LoginReq req) {
        return userRepository.findByPhone(req.getPhone()).map(user -> {
            if(BCrypt.checkpw(req.getPassword(), user.getPassword())) {
                return ResVo.success(JwtTokenUtil.generateToken(user.getId(), req.getTerminal(), jwtKeyProperties.getKey().getBytes(StandardCharsets.UTF_8)));
            }else {
                return ResVo.error(messageSource.getMessage("1001", null, locale));
            }
        }).defaultIfEmpty(ResVo.error(messageSource.getMessage("1002", null, locale)));
    }

}
