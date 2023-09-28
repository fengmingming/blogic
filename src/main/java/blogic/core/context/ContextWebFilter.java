package blogic.core.context;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Locale;

public class ContextWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Locale locale = exchange.getRequest().getHeaders().getAcceptLanguageAsLocales().stream().findFirst().orElseGet(() -> Locale.getDefault());
        return chain.filter(exchange).contextWrite(Context.of(Locale.class, locale));
    }

}
