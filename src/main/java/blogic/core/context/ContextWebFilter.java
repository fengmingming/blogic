package blogic.core.context;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Locale;

public class ContextWebFilter implements WebFilter {

    public static final String ATTRIBUTE_KEY_LOCALE = ContextWebFilter.class.getName() + ".LOCALE";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Locale locale = exchange.getLocaleContext().getLocale();
        if(locale == null) {
            locale = Locale.getDefault();
        }
        exchange.getAttributes().put(ATTRIBUTE_KEY_LOCALE, locale);
        Locale localeFinal = locale;
        return chain.filter(exchange).contextWrite(c -> c.put(Locale.class, localeFinal));
    }

}
