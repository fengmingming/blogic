package blogic.productline.bug.mapstruct;

import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.service.BugService;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BugMapStruct {

    public Bug mapToDomain(BugService.CreateBugCommand command);

    public void updateDomain(BugService.UpdateBugCommand command,@MappingTarget Bug bug);

}
