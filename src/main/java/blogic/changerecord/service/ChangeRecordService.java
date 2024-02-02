package blogic.changerecord.service;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.changerecord.domain.QChangeRecord;
import blogic.changerecord.domain.repository.ChangeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChangeRecordService {

    @Autowired
    private ChangeRecordRepository changeRecordRepository;

    public Flux<ChangeRecord> findChangeRecords(KeyTypeEnum keyType, Long primaryKey) {
        QChangeRecord qCR = QChangeRecord.changeRecord;
        return changeRecordRepository.query(q -> q.select(qCR).from(qCR)
                .where(qCR.keyType.eq(keyType.getCode()).and(qCR.primaryKey.eq(primaryKey)))).all();
    }

}
