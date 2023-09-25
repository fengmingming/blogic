package blogic.core.security;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Setter
@RequiredArgsConstructor
@Slf4j
public class AuthenticateFilter implements WebFilter {

    private final RoleAndPermissionsRepository roleAndPermissionsRepository;
    private final PermitUrlRepository permitUrlRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        //permitUrlRepository.findFuncTrees().map(ft -> );
        return chain.filter(exchange);
    }

}
