package blogic.user.domain;

import blogic.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    private Long id;
    @Column("phone")
    @NotBlank
    private String phone;
    @Column("name")
    private String name;
    @Column("password")
    private String password;

}
