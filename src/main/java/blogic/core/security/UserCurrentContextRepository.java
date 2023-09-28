package blogic.core.security;

import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public interface UserCurrentContextRepository {

    public Mono<Void> save(TokenInfo tokenInfo, UserCurrentContext context, int idleTime, TimeUnit timeUnit);

    public Mono<UserCurrentContext> find(TokenInfo tokenInfo);

    public Mono<UserCurrentContext> findAndRefreshIdleTime(TokenInfo tokenInfo);

    public Mono<Void> delete(TokenInfo tokenInfo);

}
