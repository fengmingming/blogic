package blogic.core.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TokenInfoAndUserCurrentContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class clazz = parameter.getParameterType();
        return clazz == TokenInfo.class || clazz == UserCurrentContext.class;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        Class clazz = parameter.getParameterType();
        if(clazz == TokenInfo.class) {
            TokenInfo tokenInfo = exchange.getAttribute(AuthenticateFilter.TOKEN_INFO_ATTRIBUTE_KEY);
            if(tokenInfo == null) {
                return Mono.error(new UnauthorizedException());
            }
            return Mono.just(tokenInfo);
        }else if(clazz == UserCurrentContext.class) {
            UserCurrentContext context = exchange.getAttribute(AuthenticateFilter.USER_CURRENT_CONTEXT_ATTRIBUTE_KEY);
            if(context == null) {
                return Mono.error(new NotFoundUserCurrentContextException());
            }
            return Mono.just(context);
        }
        return Mono.empty();
    }

}
