package blogic.user.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.user.domain.repository.UserDepartmentRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Setter
@Getter
@Table("user_department")
public class UserDepartment extends ActiveRecord<UserDepartment, Long> {

    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("department_id")
    private Long departmentId;

    @Override
    protected ReactiveCrudRepository<UserDepartment, Long> findRepository() {
        return SpringContext.getBean(UserDepartmentRepository.class);
    }

    @Override
    protected <S extends UserDepartment> S selfS() {
        return (S) this;
    }

}
