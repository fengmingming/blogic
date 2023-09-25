package blogic.core.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class TokenInfo {

    static final String USER_ID = "userId";
    static final String TERMINAL_TYPE = "terminal";

    private Long userId;
    private TerminalTypeEnum terminal;
    private Set<String> authorities;

}
