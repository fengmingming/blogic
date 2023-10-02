package blogic.im.domain;

import blogic.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("im_group")
public class IMGroup extends BaseEntity {

    @Id
    private Long id;
    @Column("group_name")
    private String groupName;

}
