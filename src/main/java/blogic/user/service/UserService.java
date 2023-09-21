package blogic.user.service;

import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Component
@Validated
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Mono<User> createUser(@Valid User user) {
        if(StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        user.setUpdateTime(user.getCreateTime());
        return userRepository.save(user);
    }

}
