package blogic.user.domain.repository;

import blogic.user.domain.QUserInvitation;
import blogic.user.domain.UserInvitation;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserInvitationRepository extends QuerydslR2dbcRepository<UserInvitation, Long> {

    default Mono<Boolean> existUserInvitation(Long companyId, String phone) {
        QUserInvitation qUserInvitation = QUserInvitation.userInvitation;
        return query(q -> q.select(qUserInvitation.id.count())
                .from(qUserInvitation)
                .where(qUserInvitation.companyId.eq(companyId).and(qUserInvitation.phone.eq(phone))))
                .one().map(it -> it > 0);
    }

}
