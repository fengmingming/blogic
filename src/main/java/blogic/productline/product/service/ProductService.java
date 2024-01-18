package blogic.productline.product.service;

import blogic.productline.product.domain.Product;
import blogic.productline.product.domain.ProductMember;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.collection.CollectionUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMemberRepository productMemberRepository;
    @Autowired
    private UserRepository userRepository;

    @Setter
    @Getter
    public static class CreateProductCommand {
        @NotNull
        private Long companyId;
        @NotBlank
        @Length(max = 254)
        private String productName;
        private String productDesc;
        @NotNull
        private Long createUserId;
        @NotNull
        @Size(min = 1)
        private List<Long> members;
    }

    @Transactional
    public Mono<Long> createProduct(@NotNull @Valid CreateProductCommand command) {
        Product product = new Product();
        product.setCompanyId(command.getCompanyId());
        product.setProductName(command.getProductName());
        product.setProductDesc(command.getProductDesc());
        product.setCreateUserId(command.getCreateUserId());
        product.setCreateTime(LocalDateTime.now());
        return productRepository.save(product).flatMap(p ->
            productMemberRepository.saveAll(p.addMembers(command.getMembers())).then(Mono.just(p.getId()))
        );
    }

    @Setter
    @Getter
    public static class UpdateProductCommand {
        @NotNull
        private Long productId;
        @NotBlank
        @Length(max = 254)
        private String productName;
        private String productDesc;
        private List<Long> members;
    }

    @Transactional
    public Mono<Void> updateProduct(@NotNull @Valid UpdateProductCommand command) {
        Mono<Product> productMono = productRepository.findById(command.getProductId());
        return productMono.doOnNext(it -> {
            it.setProductName(command.getProductName());
            it.setProductDesc(command.getProductDesc());
            it.setUpdateTime(LocalDateTime.now());
        })
        .flatMap(it -> productRepository.save(it))
        .flatMap(p -> {
            Flux<ProductMember> members = p.findMembers();
            return members.collectList().flatMap(its -> {
                List<Long> userIds = its.stream().map(it -> it.getUserId()).collect(Collectors.toList());
                List<Long> addUserIds = CollectionUtil.subtractToList(command.getMembers(), userIds);
                List<Long> removedUserIds = CollectionUtil.subtractToList(userIds, command.getMembers());
                return productMemberRepository.saveAll(p.addMembers(addUserIds))
                .then(productMemberRepository.deleteAll(p.removeMembers(removedUserIds)));
            });
        });
    }

    @Transactional
    public Mono<Void> deleteProduct(Long productId) {
        return productRepository.update(update ->
                update.set(QProduct.product.deleted, true)
                        .where(QProduct.product.id.eq(productId))).then();
    }

}
