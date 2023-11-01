package blogic.core.security;

import blogic.core.exception.ForbiddenAccessException;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class AuthenticateFilter implements WebFilter {

    static final String TOKEN_INFO_ATTRIBUTE_KEY = AuthenticateFilter.class.getName() + ".TOKEN_INFO";
    static final String USER_CURRENT_CONTEXT_ATTRIBUTE_KEY = AuthenticateFilter.class.getName() + ".USER_CURRENT_CONTEXT";
    private final RoleAndPermissionsRepository roleAndPermissionsRepository;
    private final PermitUrlRepository permitUrlRepository;
    private final JwtKeyProperties jwtKeyProperties;
    private final UserCurrentContextRepository userCurrentContextRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String funcUrl = buildFuncUrl(request.getMethod().name(), request.getPath().value(), request.getQueryParams());
        FuncTrees reqFT = FuncTrees.buildFuncTrees(Arrays.asList(funcUrl));
        Mono<Void> authenticateMono = authenticate(exchange, reqFT).switchIfEmpty(Mono.just(false)).flatMap(it -> {
            if(it) {
                return chain.filter(exchange);
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
        return permitUrlRepository.findFuncTrees().flatMap(fts -> {
            Optional<FuncTree.Authorities> authoritiesOpt = FuncTrees.match(fts, reqFT.firstFuncTree().get());
            if(authoritiesOpt.isPresent()) {
                return chain.filter(exchange);
            }else {
                return authenticateMono;
            }
        });
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
        if(StrUtil.isBlank(authorization)) return Mono.error(new UnauthorizedException());
        String token = JwtTokenUtil.getTokenFromAuthorization(authorization);
        if(!JwtTokenUtil.validToken(token, jwtKeyProperties.getKey().getBytes(StandardCharsets.UTF_8))) {
            return Mono.error(new UnauthorizedException());
        }
        TokenInfo tokenInfo = JwtTokenUtil.getTokenInfo(token);
        exchange.getAttributes().putIfAbsent(TOKEN_INFO_ATTRIBUTE_KEY, tokenInfo);
        return userCurrentContextRepository.findAndRefreshIdleTime(tokenInfo).flatMap(current -> {
            exchange.getAttributes().putIfAbsent(USER_CURRENT_CONTEXT_ATTRIBUTE_KEY, current);
            return roleAndPermissionsRepository.findFuncTrees(tokenInfo.getUserId()).map(fts -> {
                Optional<FuncTree.Authorities> authoritiesOpt = FuncTrees.match(fts, reqFT.firstFuncTree().get());
                if(!authoritiesOpt.isPresent()) return true;
                return CollectionUtil.intersection(authoritiesOpt.get().getAuthorities(),
                        current.getAuthorities().stream().map(it -> it.name()).collect(Collectors.toSet())).size() > 0;
            });
        });
    }

    @ConfigurationProperties(prefix = "blogic.jwt")
    @Getter
    @Setter
    public static class JwtKeyProperties {
        private String key;
        private Integer timeout = 30;
    }

}
