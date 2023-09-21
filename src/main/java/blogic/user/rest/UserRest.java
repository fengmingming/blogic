package blogic.user.rest;

import blogic.core.rest.ResVo;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class UserRest {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/Users")
    public Mono<ResVo<List<User>>> getUsers() {
        return Mono.error(new RuntimeException("users"));
    }

}
