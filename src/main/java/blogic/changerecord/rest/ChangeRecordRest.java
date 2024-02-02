package blogic.changerecord.rest;

import blogic.changerecord.domain.ChangeRecord;
import blogic.changerecord.domain.KeyTypeEnum;
import blogic.changerecord.service.ChangeRecordService;
import blogic.core.enums.DigitalizedEnumPropertyEditor;
import blogic.core.rest.ResVo;
import blogic.core.security.UserCurrentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class ChangeRecordRest {

    @Autowired
    private ChangeRecordService changeRecordService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(KeyTypeEnum.class, new DigitalizedEnumPropertyEditor(KeyTypeEnum.class));
    }

    @GetMapping("/Companies/{companyId}/ChangeRecords")
    public Mono<ResVo<List<ChangeRecord>>> findChangeRecords(@PathVariable("companyId") Long companyId, UserCurrentContext context,
                                                      @RequestParam("primaryKey") Long primaryKey,
                                                      @RequestParam("keyType") KeyTypeEnum keyType) {
        context.equalsCompanyIdOrThrowException(companyId);
        return changeRecordService.findChangeRecords(keyType, primaryKey).collectList().map(ResVo::success);
    }

}