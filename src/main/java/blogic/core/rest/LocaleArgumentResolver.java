package blogic.core.rest;


import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public class LocaleArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Locale.class;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        List<Locale> locales = exchange.getRequest().getHeaders().getAcceptLanguageAsLocales();
        return locales.size() > 0? Mono.just(locales.get(0)): Mono.just(Locale.getDefault());
    }

}
