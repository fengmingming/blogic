package blogic.core.domain;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BaseEntity {

    @Column(name = "create_time", updatable = false)
    @NotNull
    private LocalDateTime createTime;
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    @Column(name = "deleted", nullable = false)
    @NotNull
    private Boolean deleted = false;

}
