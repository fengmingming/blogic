package blogic.user.rest;

import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class UserRest {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/Users")
    public Flux<User> getUsers() {
        return userRepository.findAll();
    }

}
