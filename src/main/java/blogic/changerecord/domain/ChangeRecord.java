package blogic.changerecord.domain;

import blogic.changerecord.domain.repository.ChangeRecordRepository;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("change_record")
public class ChangeRecord extends ActiveRecord<ChangeRecord, Long> {

    @Id
    private Long id;
    @Column("primary_key")
    private Long primaryKey;
    @Column("key_type")
    private Integer keyType;
    @Column("oper_user_id")
    private Long operUserId;
    @Column("oper_desc")
    private String operDesc;
    @Column("note")
    private String note;
    @Column("create_time")
    private LocalDateTime createTime;

    @Override
    protected ReactiveCrudRepository<ChangeRecord, Long> findRepository() {
        return SpringContext.getBean(ChangeRecordRepository.class);
    }

    @Override
    protected <S extends ChangeRecord> S selfS() {
        return (S) this;
    }

}
