package blogic.core.security;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@EqualsAndHashCode(exclude = "createTime")
public class TokenInfo {

    static final String USER_ID = "userId";
    static final String TERMINAL_TYPE = "terminal";
    static final String CREATE_TIME = "createTime";

    private Long userId;
    private TerminalTypeEnum terminal;
    private LocalDateTime createTime;

}
