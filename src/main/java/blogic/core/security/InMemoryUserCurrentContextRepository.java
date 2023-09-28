package blogic.core.security;

import cn.hutool.cache.impl.TimedCache;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 基于内存的上下文信息存储库
 * */
public class InMemoryUserCurrentContextRepository implements UserCurrentContextRepository{

    private TimedCache<TokenInfo, UserCurrentContext> cache = new TimedCache<>(TimeUnit.MILLISECONDS.toMinutes(30));

    @Override
    public Mono<Void> save(TokenInfo tokenInfo, UserCurrentContext context, int idleTime, TimeUnit timeUnit) {
        cache.put(tokenInfo, context, timeUnit.toMillis(idleTime));
        return Mono.empty();
    }

    @Override
    public Mono<UserCurrentContext> find(TokenInfo tokenInfo) {
        UserCurrentContext context = cache.get(tokenInfo, false);
        if(context == null) return Mono.empty();
        return Mono.just(context);
    }

    @Override
    public Mono<UserCurrentContext> findAndRefreshIdleTime(TokenInfo tokenInfo) {
        UserCurrentContext context = cache.get(tokenInfo, true);
        if(context == null) return Mono.empty();
        return Mono.just(context);
    }

    @Override
    public Mono<Void> delete(TokenInfo tokenInfo) {
        cache.remove(tokenInfo);
        return Mono.empty();
    }

}
