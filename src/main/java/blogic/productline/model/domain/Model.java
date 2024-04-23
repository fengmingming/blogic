package blogic.productline.model.domain;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.core.ObjectsTool;
import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.productline.model.domain.repository.ModelRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("model")
public class Model extends ActiveRecord<Model, Long> {

    @Id
    private Long id;
    @Column("product_id")
    private Long productId;
    @Column("name")
    private String name;
    @Column("data")
    private String data;
    @Column("create_time")
    private LocalDateTime createTime;
    @Column("update_time")
    private LocalDateTime updateTime;

    @Override
    protected ReactiveCrudRepository<Model, Long> findRepository() {
        return SpringContext.getBean(ModelRepository.class);
    }

    @Override
    protected <S extends Model> S selfS() {
        return (S) this;
    }

}