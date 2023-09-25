package blogic.core.security;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * permit urls repository
 * */
public interface PermitUrlRepository {
    
    public Flux<String> findAll(Long userId);

    default Mono<FuncTrees> findFuncTrees(Long userId) {
        return FuncTrees.buildFuncTrees(findAll(userId));
    }

}
