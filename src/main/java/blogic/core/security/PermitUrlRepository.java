package blogic.core.security;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * permit urls repository
 * */
public interface PermitUrlRepository {
    
    public Flux<String> findAll();

    default Mono<FuncTrees> findFuncTrees() {
        return FuncTrees.buildFuncTrees(findAll());
    }

}
