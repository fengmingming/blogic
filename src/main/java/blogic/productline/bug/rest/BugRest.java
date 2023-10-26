package blogic.productline.bug.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.bug.domain.BugStatusEnum;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.bug.service.BugService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BugRest {

    @Autowired
    private BugRepository bugRepository;
    @Autowired
    private BugService bugService;

    @Setter
    @Getter
    public static class FindBugsReq {
        private Long iterationId;
        private Long requirementId;
        private BugStatusEnum status;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Bugs")
    public Mono<ResVo<?>> findBugs(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                   TokenInfo tokenInfo, UserCurrentContext context, FindBugsReq req) {
        return null;
    }

}
