package blogic.productline.product.service;

import blogic.productline.product.domain.Product;
import blogic.productline.product.domain.ProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
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
        Flux<User> users = userRepository.findAllById(command.getMembers());
        return productRepository.save(product).flatMap(p -> users.collectList()
            .map(its -> p.addMembers(its))
            .flatMapMany(members -> productMemberRepository.saveAll(members))
            .then(Mono.just(p.getId())));
    }

}
