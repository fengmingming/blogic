package blogic.productline.product.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("product_member")
public class ProductMember {

    @Id
    private Long id;
    @Column("user_id")
    @NotNull
    private Long userId;
    @Column("product_id")
    @NotNull
    private Long productId;

}
