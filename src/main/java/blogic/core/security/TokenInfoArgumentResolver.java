package blogic.core.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TokenInfoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == TokenInfo.class;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        TokenInfo tokenInfo = exchange.getAttribute(AuthenticateFilter.TOKEN_INFO_ATTRIBUTE_KEY);
        if(tokenInfo == null) {
            return Mono.error(new UnauthorizedException());
        }
        return Mono.just(tokenInfo);
    }

}
