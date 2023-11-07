package blogic.productline.bug.mapstruct;

import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.domain.BugStatusEnum;
import blogic.productline.bug.rest.BugRest;
import blogic.productline.bug.service.BugService;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BugMapStruct {

    public Bug mapToDomain(BugService.CreateBugCommand command);

    @Mappings(@Mapping(target = "status", expression = "java(findCode(command.getStatus()))"))
    public void updateDomain(BugService.UpdateBugCommand command,@MappingTarget Bug bug);

    default Integer findCode(BugStatusEnum statusEnum) {
        if(statusEnum == null) return null;
        return statusEnum.getCode();
    }

    public BugService.CreateBugCommand mapToCommand(BugRest.CreateBugReq req);

    public BugService.UpdateBugCommand mapToCommand(BugRest.UpdateBugReq req);

}
