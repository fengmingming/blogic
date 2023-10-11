package blogic.productline.product.domain;

import blogic.core.context.SpringContext;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import com.querydsl.core.types.Predicate;
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

    public Flux<ProductMember> findMembers() {
        if(this.id == null) return Flux.empty();
        Predicate predicate = QProductMember.productMember.productId.eq(this.id);
        return SpringContext.INSTANCE().getBean(ProductMemberRepository.class).query(query ->
            query.select(QProductMember.productMember.getProjection()).from(QProductMember.productMember).where(predicate)).all();
    }

    public Collection<ProductMember> addMembers(Collection<Long> users) {
        Assert.notNull(this.id, "Product is not persistent, Product.id is null");
        return users.stream().map(it -> {
            ProductMember member = new ProductMember();
            member.setProductId(this.id);
            member.setUserId(it);
            return member;
        }).collect(Collectors.toList());
    }

    public Flux<ProductMember> removeMembers(Collection<Long> removedUserIds) {
        return findMembers().collectList().flatMapMany(its ->
                Flux.fromStream(its.stream().filter(it -> removedUserIds.contains(it.getUserId()))));
    }

}
