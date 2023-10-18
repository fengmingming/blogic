package blogic.company.domain;

import blogic.company.domain.repository.DepartmentRepository;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("department")
public class Department extends ActiveRecord<Department, Long> {

    @Id
    private Long id;
    @Column("company_id")
    private Long companyId;
    @Column("department_name")
    private String departmentName;
    @Column("parent_id")
    private Long parentId;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<Department, Long> findRepository() {
        return SpringContext.getBean(DepartmentRepository.class);
    }

    @Override
    protected <S extends Department> S selfS() {
        return (S) this;
    }

}
