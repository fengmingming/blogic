package blogic.user.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.user.domain.repository.UserCompanyRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("user_company")
public class UserCompany extends ActiveRecord<UserCompany, Long> {

    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("company_id")
    private Long companyId;
    @Column("def")
    private Boolean def;
    @Column("def_product_id")
    private Long defProductId;
    @Column("create_time")
    private LocalDateTime createTime;

    @Override
    protected ReactiveCrudRepository<UserCompany, Long> findRepository() {
        return SpringContext.getBean(UserCompanyRepository.class);
    }

    @Override
    protected <S extends UserCompany> S selfS() {
        return (S) this;
    }

}
