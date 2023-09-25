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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        return chain.filter(exchange);
    }

}
