package blogic.company.domain;

import blogic.company.domain.repository.CompanyRepository;
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
@Table("company")
public class Company extends ActiveRecord<Company, Long> {

    @Id
    private Long id;
    @Column("company_name")
    @NotNull
    private String companyName;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    @Override
    protected ReactiveCrudRepository<Company, Long> findRepository() {
        return SpringContext.getBean(CompanyRepository.class);
    }

    @Override
    protected <S extends Company> S selfS() {
        return (S) this;
    }

}
