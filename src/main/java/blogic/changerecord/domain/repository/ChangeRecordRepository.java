package blogic.changerecord.domain.repository;

import blogic.changerecord.domain.ChangeRecord;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeRecordRepository extends QuerydslR2dbcRepository<ChangeRecord, Long> {

}
