package blogic;

import blogic.core.bean.PhysicalNamingStrategy;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@SpringBootApplication
@Slf4j
public class BLogicBootstrap {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        SpringApplication.run(BLogicBootstrap.class, args);
        log.info("bootstrap success " + (System.currentTimeMillis() - time));
    }

    @Bean
    public SQLTemplates sqlTemplates() {
        return new MySQLTemplates();
    }

    @Bean
    public NamingStrategy namingStrategy() {
        return new PhysicalNamingStrategy();
    }

    @Bean
    @Order(90)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/*", config);
        return new CorsWebFilter(configSource);
    }

}
