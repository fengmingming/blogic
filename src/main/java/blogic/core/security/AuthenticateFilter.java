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
@RequiredArgsConstructor
@Slf4j
public class AuthenticateFilter implements WebFilter {

    static final String TOKEN_INFO_ATTRIBUTE_KEY = AuthenticateFilter.class.getName() + ".TOKEN_INFO";
    private final RoleAndPermissionsRepository roleAndPermissionsRepository;
    private final PermitUrlRepository permitUrlRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String funcUrl = buildFuncUrl(request.getMethod().name(), request.getPath().value(), request.getQueryParams());
        FuncTrees reqFT = FuncTrees.buildFuncTrees(Arrays.asList(funcUrl));
        Mono<Void> authenticateMono = authenticate(exchange, reqFT).flatMap(it -> {
            if(it) {
                return chain.filter(exchange);
            }else {
                return Mono.error(new UnauthorizedException());
            }
        });
        return permitUrlRepository.findFuncTrees().flatMap(fts -> {
            Optional<FuncTree.Authorities> authoritiesOpt = FuncTrees.match(fts, reqFT.firstFuncTree().get());
            if(authoritiesOpt.isPresent()) {
                return chain.filter(exchange);
            }else {
                return authenticateMono;
            }
        }).switchIfEmpty(authenticateMono);
    }

    protected String buildFuncUrl(String method, String path, Map<String, List<String>> params) {
        StringBuilder sb = new StringBuilder(method);
        sb.append(":").append(path).append("?");
        params.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(value -> {
                sb.append(entry.getKey()).append("=").append(value).append("&");
            });
        });
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    protected Mono<Boolean> authenticate(ServerWebExchange exchange, FuncTrees reqFT) {
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if(StrUtil.isBlank(authorization)) return Mono.just(Boolean.FALSE);
        String token = JwtTokenUtil.getTokenFromAuthorization(authorization);
        TokenInfo tokenInfo = JwtTokenUtil.getTokenInfo(token);
        exchange.getAttributes().putIfAbsent(TOKEN_INFO_ATTRIBUTE_KEY, tokenInfo);
        return roleAndPermissionsRepository.findFuncTrees(tokenInfo.getUserId()).map(fts -> {
            Optional<FuncTree.Authorities> authoritiesOpt = FuncTrees.match(fts, reqFT.firstFuncTree().get());
            if(!authoritiesOpt.isPresent()) return false;
            return CollectionUtil.intersection(authoritiesOpt.get().getAuthorities(), tokenInfo.getAuthorities()).size() > 0;
        }).defaultIfEmpty(true);
    }

}
