package blogic.company.domain;

import blogic.core.domain.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("company")
public class Company extends BaseEntity {

    @Id
    private Long id;
    @Column("company_name")
    private String companyName;

}
