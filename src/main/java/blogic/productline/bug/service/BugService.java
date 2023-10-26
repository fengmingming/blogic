package blogic.productline.bug.service;

import blogic.productline.bug.domain.Bug;
import blogic.productline.bug.domain.repository.BugRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class BugService {

    @Autowired
    private BugRepository bugRepository;

    @Setter
    @Getter
    public static class CreateBugCommand {

    }

    @Transactional
    public Mono<Bug> createBug(@NotNull @Valid CreateBugCommand command) {
        return null;
    }

    @Setter
    @Getter
    public static class UpdateBugCommand {

    }

    @Transactional
    public Mono<Bug> updateBug(@NotNull @Valid UpdateBugCommand command) {
        return null;
    }

}
