package blogic.core.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JwtToken生成的工具类
 * Created on 2022/6/22.
 */
@Slf4j
public class JwtTokenUtil {

    /**
     * 根据负责生成JWT的token
     */
    public static String generateToken(Long userId, TerminalTypeEnum terminal, byte[] key) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(terminal);
        Objects.requireNonNull(key);
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put(TokenInfo.USER_ID, userId);
        tokenInfo.put(TokenInfo.TERMINAL_TYPE, terminal);
        return JWTUtil.createToken(tokenInfo, key);
    }

    static boolean validToken(String token, byte[] key) {
        return JWTUtil.verify(token, key);
    }

    static String getTokenFromAuthorization(String authorization) {
        if(StrUtil.isNotBlank(authorization) && authorization.length() > 8 && authorization.startsWith("Bearer ")){
            return authorization.substring(8).trim();
        }
        throw new IllegalArgumentException("Authorization format is incorrect");
    }

    static TokenInfo getTokenInfo(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        JWTPayload payload = jwt.getPayload();
        return TokenInfo.builder().userId(payload.getClaimsJson().getLong(TokenInfo.USER_ID))
                .terminal(TerminalTypeEnum.valueOf(payload.getClaimsJson().getStr(TokenInfo.TERMINAL_TYPE)))
                .build();
    }

}
