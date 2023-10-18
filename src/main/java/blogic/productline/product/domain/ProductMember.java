package blogic.productline.product.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Setter
@Getter
@Table("product_member")
public class ProductMember extends ActiveRecord<ProductMember, Long> {

    @Id
    private Long id;
    @Column("user_id")
    @NotNull
    private Long userId;
    @Column("product_id")
    @NotNull
    private Long productId;

    @Override
    protected ReactiveCrudRepository<ProductMember, Long> findRepository() {
        return SpringContext.getBean(ProductMemberRepository.class);
    }

    @Override
    protected <S extends ProductMember> S selfS() {
        return (S) this;
    }

}
