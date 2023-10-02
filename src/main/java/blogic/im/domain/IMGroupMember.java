package blogic.im.domain;

import blogic.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("im_group_member")
public class IMGroupMember extends BaseEntity {

    @Id
    private Long id;
    @Column("group_id")
    private Long groupId;
    @Column("user_id")
    private Long userId;
    @Column("admin")
    private Boolean admin;

}
