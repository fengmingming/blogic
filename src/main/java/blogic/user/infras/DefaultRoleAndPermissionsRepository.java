package blogic.user.infras;

import blogic.core.security.RoleAndPermissionsRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DefaultRoleAndPermissionsRepository implements RoleAndPermissionsRepository {

    @Override
    public Flux<String> findAllByUserId(Long userId) {
        return Flux.empty();
    }

}
