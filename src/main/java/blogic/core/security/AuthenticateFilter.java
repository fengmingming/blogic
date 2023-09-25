package blogic.core.security;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Setter
@RequiredArgsConstructor
@Slf4j
public class AuthenticateFilter implements WebFilter {

    private final RoleAndPermissionsRepository roleAndPermissionsRepository;
    private final PermitUrlRepository permitUrlRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String funcUrl = buildFuncUrl(request.getMethod().name(), request.getPath().value(), request.getQueryParams());
        FuncTrees reqFT = FuncTrees.buildFuncTrees(Arrays.asList(funcUrl));
        permitUrlRepository.findFuncTrees();
        return chain.filter(exchange);
    }

    protected String buildFuncUrl(String method, String path, Map<String, List<String>> params) {
        StringBuilder sb = new StringBuilder(method);
        sb.append(":").append(path).append("?");

        return sb.toString();
    }

    protected Mono<Boolean> authenticate(ServerHttpRequest request, FuncTrees reqFT) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if(StrUtil.isBlank(authorization)) return Mono.just(Boolean.FALSE);
        String token = JwtTokenUtil.getTokenFromAuthorization(authorization);
        TokenInfo tokenInfo = JwtTokenUtil.getTokenInfo(token);
        return roleAndPermissionsRepository.findFuncTrees(tokenInfo.getUserId()).map(fts -> {
            Optional<FuncTree.Authorities> authoritiesOpt = FuncTrees.match(fts, reqFT.firstFuncTree().get());
            if(!authoritiesOpt.isPresent()) return false;
            return CollectionUtil.intersection(authoritiesOpt.get().getAuthorities(), tokenInfo.getAuthorities()).size() > 0;
        });
    }

}
