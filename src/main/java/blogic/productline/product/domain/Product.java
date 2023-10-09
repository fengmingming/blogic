package blogic.productline.product.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.BaseEntity;
import blogic.productline.product.domain.repository.ProductRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;

@Setter
@Getter
@Table("product")
public class Product extends BaseEntity {

    @Id
    private Long id;
    @Column("company_id")
    private Long companyId;
    @Column("product_name")
    private String productName;
    @Column("product_desc")
    private String productDesc;
    @Column("create_user_id")
    private Long createUserId;

    public Flux<Long> getMembers() {
        if(this.id == null) return Flux.empty();
        return SpringContext.INSTANCE().getBean(ProductRepository.class).findMembersByProductId(this.id);
    }

}
