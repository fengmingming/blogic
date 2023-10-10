package blogic.productline.product.domain;

import blogic.core.context.SpringContext;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.user.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Setter
@Getter
@Table("product")
public class Product {

    @Id
    private Long id;
    @Column("company_id")
    @NotNull
    private Long companyId;
    @Column("product_name")
    @NotBlank
    @Length(max = 254)
    private String productName;
    @Column("product_desc")
    private String productDesc;
    @Column("create_user_id")
    @NotNull
    private Long createUserId;
    @Column("create_time")
    @NotNull
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;
    @Column("deleted")
    @NotNull
    private Boolean deleted = false;

    public Flux<Long> findMembers() {
        if(this.id == null) return Flux.empty();
        return SpringContext.INSTANCE().getBean(ProductRepository.class).findMembersByProductId(this.id);
    }

    public Collection<ProductMember> addMembers(Collection<User> users) {
        Assert.notNull(this.id, "Product is not persistent, Product.id is null");
        return users.stream().map(it -> {
            ProductMember member = new ProductMember();
            member.setProductId(this.id);
            member.setUserId(it.getId());
            return member;
        }).collect(Collectors.toList());
    }

}
