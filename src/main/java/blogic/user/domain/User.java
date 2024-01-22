package blogic.user.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.core.exception.BExecConstraintException;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Setter
@Getter
@Table(name = "user")
public class User extends ActiveRecord<User, Long> {

    @Id
    private Long id;
    @Column("phone")
    @NotBlank
    private String phone;
    @Column("name")
    private String name;
    @Column("password")
    private String password;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<User, Long> findRepository() {
        return SpringContext.getBean(UserRepository.class);
    }

    @Override
    protected <S extends User> S selfS() {
        return (S) this;
    }

    public void updatePassword(String oldPassword, String newPassword) {
        if(!BCrypt.checkpw(oldPassword, getPassword())) {
            throw new BExecConstraintException("the oldPassword is wrong");
        }
        setPassword(BCrypt.hashpw(newPassword));
    }

}
