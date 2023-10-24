package blogic.core.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration(proxyBeanMethods = false)
public class ContextConfiguration {

    @Bean
    public WebFilter contextWebFilter() {
        return new ContextWebFilter();
    }

}
