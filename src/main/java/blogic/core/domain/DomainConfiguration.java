package blogic.core.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DomainConfiguration {

    @Bean
    public DomainLogicConsistencyHandler domainBeforeSaveHandler() {
        return new DomainLogicConsistencyHandler();
    }

}
