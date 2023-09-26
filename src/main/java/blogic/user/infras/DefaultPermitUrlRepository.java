package blogic.user.infras;

import blogic.core.security.PermitUrlRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultPermitUrlRepository implements PermitUrlRepository {

    private final BlogicProperties blogicProperties;

    @Override
    public Flux<String> findAll() {
        return Flux.fromIterable(blogicProperties.getPermitUrls());
    }

    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "blogic")
    @Getter
    @Setter
    public static class BlogicProperties {
        private List<String> permitUrls = new ArrayList<>();
    }

}
