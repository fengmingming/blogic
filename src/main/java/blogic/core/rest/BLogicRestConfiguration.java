package blogic.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties
@Configuration
public class BLogicRestConfiguration {

    @Bean
    public ErrorAttributes defaultErrorAttributes(@Autowired MessageSource codedExceptionMessageSource) {
        return new DefaultErrorAttributes(codedExceptionMessageSource);
    }

}
