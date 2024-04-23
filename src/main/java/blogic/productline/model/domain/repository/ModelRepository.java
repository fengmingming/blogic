package blogic.productline.model.domain.repository;

import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface ModelRepository extends QuerydslR2dbcRepository<Model, Long> {

}
