package blogic.core.security;

import cn.hutool.cache.impl.TimedCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 基于内存的上下文信息存储库
 * */
@Component
@ConditionalOnMissingBean
public class InMemoryUserCurrentContextRepository implements UserCurrentContextRepository{

    private TimedCache<TokenInfo, UserCurrentContext> cache = new TimedCache<>(TimeUnit.MILLISECONDS.toMinutes(30));

    @Override
    public Mono<Void> save(TokenInfo tokenInfo, UserCurrentContext context, int idleTime, TimeUnit timeUnit) {
        cache.put(tokenInfo, context, timeUnit.toMillis(idleTime));
        return Mono.empty();
    }

    @Override
    public Mono<UserCurrentContext> find(TokenInfo tokenInfo) {
        return Mono.just(cache.get(tokenInfo, false));
    }

    @Override
    public Mono<UserCurrentContext> findAndRefreshIdleTime(TokenInfo tokenInfo) {
        return Mono.just(cache.get(tokenInfo, true));
    }

    @Override
    public Mono<Void> delete(TokenInfo tokenInfo) {
        cache.remove(tokenInfo);
        return Mono.empty();
    }

}
