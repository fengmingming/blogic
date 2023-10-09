package blogic;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

}
