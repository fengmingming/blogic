package blogic.company.domain;

import blogic.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("department")
public class Department extends BaseEntity {

    @Id
    private Long id;
    @Column("company_id")
    private Long companyId;
    @Column("department_name")
    private String departmentName;
    @Column("parent_id")
    private Long parentId;

}
