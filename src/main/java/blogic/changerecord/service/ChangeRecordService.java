package blogic.changerecord.service;

import blogic.changerecord.domain.repository.ChangeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangeRecordService {

    @Autowired
    private ChangeRecordRepository changeRecordRepository;

}
