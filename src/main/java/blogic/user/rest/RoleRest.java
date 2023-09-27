package blogic.user.rest;

import blogic.core.rest.ResVo;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
public class RoleRest {

    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;

    @GetMapping("/Roles")
    public Mono<ResVo<List<String>>> getRoles() {
        return Flux.fromStream(Arrays.stream(RoleEnum.values()))
                .map(it -> it.name()).collectList()
                .map(it -> ResVo.success(it));
    }

}
