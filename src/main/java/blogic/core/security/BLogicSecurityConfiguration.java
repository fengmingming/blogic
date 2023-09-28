package blogic.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@EnableConfigurationProperties
@Configuration
public class BLogicSecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserCurrentContextRepository userCurrentContextRepository() {
        return new InMemoryUserCurrentContextRepository();
    }

    @Bean
    public AuthenticateFilter.JwtKeyProperties jwtKeyProperties() {
        return new AuthenticateFilter.JwtKeyProperties();
    }

    @Bean
    public AuthenticateFilter authenticateFilter(@Autowired RoleAndPermissionsRepository roleAndPermissionsRepository,
                                                 @Autowired PermitUrlRepository permitUrlRepository,
                                                 @Autowired AuthenticateFilter.JwtKeyProperties jwtKeyProperties,
                                                 @Autowired UserCurrentContextRepository userCurrentContextRepository) {
        return new AuthenticateFilter(roleAndPermissionsRepository, permitUrlRepository, jwtKeyProperties, userCurrentContextRepository);
    }

    @Bean
    public WebFluxConfigurer authenticateWebFluxConfigurer() {
        return new AuthenticateWebFluxConfigurer();
    }

}
