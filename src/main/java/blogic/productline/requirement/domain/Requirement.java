package blogic.productline.requirement.domain;

import blogic.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("requirement")
public class Requirement extends BaseEntity {

    @Id
    private Long id;
    @Column("product_id")
    private Long productId;
    @Column("requirement_name")
    private String requirementName;
    @Column("requirement_sources")
    private String requirementSources;
    @Column("requirement_desc")
    private String requirementDesc;
    @Column("requirement_status")
    private Integer requirementStatus;
    @Column("create_user_id")
    private Long createUserId;

    public RequirementStatus getRequirementStatusEnum() {
        if(this.requirementStatus == null) return null;
        return RequirementStatus.findByCode(this.requirementStatus);
    }

    public void setRequirementStatusEnum(RequirementStatus requirementStatus) {
        this.requirementStatus = requirementStatus.getCode();
    }

}
