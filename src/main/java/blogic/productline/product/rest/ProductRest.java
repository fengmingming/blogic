package blogic.productline.product.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.product.service.ProductService;
import blogic.productline.requirement.domain.QRequirement;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class ProductRest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/Company/{companyId}/Products")
    public Mono<ResVo<?>> findProducts(@PathVariable("companyId")Long companyId, TokenInfo tokenInfo,
                                       UserCurrentContext context, @RequestBody Paging paging) {
        context.equalsCompanyIdOrThrowException(companyId);
        if(context.authenticate(RoleEnum.ROLE_MANAGER)) {
            QProduct qProduct = QProduct.product;
            return productRepository.query(query -> query.select(qProduct).from(qProduct).where(qProduct.companyId.eq(companyId))
                .offset(paging.getOffset()).limit(paging.getLimit())).all().collectList().flatMap(products -> {
                    List<Long> userIds = products.stream().map(it -> it.getCreateUserId()).collect(Collectors.toList());
                    return userRepository.findAllById(userIds).collectList().map(users -> {
                        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
                        return products.stream().map(it -> JSONUtil.parseObj(it)
                            .putOnce("userName", userMap.get(it.getCreateUserId()).getName())).collect(Collectors.toList());
                    });
                }).map(it -> ResVo.success(it));
        }else {

        }
        return Mono.just(ResVo.success());
    }

}
