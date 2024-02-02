package blogic.changerecord.domain;

import blogic.changerecord.domain.repository.ChangeRecordRepository;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Override
    protected ReactiveCrudRepository<ChangeRecord, Long> findRepository() {
        return SpringContext.getBean(ChangeRecordRepository.class);
    }

    @Override
    protected <S extends ChangeRecord> S selfS() {
        return (S) this;
    }

    public static ChangeRecordBuilder builder() {
        return new ChangeRecordBuilder();
    }

    public static class ChangeRecordBuilder {
        private Long id;
        private Long primaryKey;
        private Integer keyType;
        private Long operUserId;
        private String operDesc;
        private String note;
        private LocalDateTime createTime;

        public ChangeRecordBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChangeRecordBuilder primaryKey(Long primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public ChangeRecordBuilder keyType(Integer keyType) {
            this.keyType = keyType;
            return this;
        }

        public ChangeRecordBuilder operUserId(Long operUserId) {
            this.operUserId = operUserId;
            return this;
        }

        public ChangeRecordBuilder operDesc(String operDesc) {
            this.operDesc = operDesc;
            return this;
        }

        public ChangeRecordBuilder note(String note) {
            this.note = note;
            return this;
        }

        public ChangeRecordBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public ChangeRecord build() {
            ChangeRecord record = new ChangeRecord();
            record.setKeyType(keyType);
            record.setPrimaryKey(primaryKey);
            record.setOperDesc(operDesc);
            record.setNote(note);
            record.setOperUserId(operUserId);
            record.setCreateTime(createTime);
            return record;
        }
    }

}
