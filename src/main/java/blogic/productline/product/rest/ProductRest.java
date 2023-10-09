package blogic.productline.product.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.product.service.ProductService;
import blogic.user.domain.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductRest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;

    @GetMapping("/Company/{companyId}/Products")
    public Mono<ResVo<?>> findProducts(@PathVariable("companyId")Long companyId, TokenInfo tokenInfo, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        if(context.authenticate(RoleEnum.ROLE_MANAGER)) {

        }else {

        }
        return Mono.just(ResVo.success());
    }

}
