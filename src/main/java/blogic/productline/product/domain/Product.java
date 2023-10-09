package blogic.productline.product.domain;

import blogic.core.context.SpringContext;
import blogic.productline.product.domain.repository.ProductRepository;
import com.querydsl.core.annotations.QuerySupertype;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("product")
@QuerySupertype
public class Product {

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
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    public Flux<Long> getMembers() {
        if(this.id == null) return Flux.empty();
        return SpringContext.INSTANCE().getBean(ProductRepository.class).findMembersByProductId(this.id);
    }

}
