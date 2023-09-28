package blogic.core.security;

import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

public class AuthenticateWebFluxConfigurer implements WebFluxConfigurer {

    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new TokenInfoAndUserCurrentContextArgumentResolver());
    }

}
