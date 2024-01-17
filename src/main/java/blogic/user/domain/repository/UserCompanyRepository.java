package blogic.user.domain.repository;

import blogic.user.domain.QUserCompany;
import blogic.user.domain.UserCompany;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserCompanyRepository extends QuerydslR2dbcRepository<UserCompany, Long> {

    default Mono<Void> resetAllDef(Long userId) {
        QUserCompany qUC = QUserCompany.userCompany;
        return update(u -> u.set(qUC.def, false).where(qUC.userId.eq(userId))).then();
    }

}
