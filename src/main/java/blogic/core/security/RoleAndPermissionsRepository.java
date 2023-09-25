package blogic.core.security;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 角色和权限Repository
 */
public interface RoleAndPermissionsRepository {

    /**
     * 根据用户id查询用户
     * */
    public Flux<String> findAllByUserId(Long userId);

    default Mono<FuncTrees> findFuncTrees(Long userId) {
        return FuncTrees.buildFuncTrees(findAllByUserId(userId));
    }

}
