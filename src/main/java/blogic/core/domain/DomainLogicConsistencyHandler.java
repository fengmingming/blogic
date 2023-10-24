package blogic.core.domain;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

public class DomainLogicConsistencyHandler implements BeforeSaveCallback<LogicConsistencyProcessor> {

    @Override
    public Publisher<LogicConsistencyProcessor> onBeforeSave(LogicConsistencyProcessor entity, OutboundRow row, SqlIdentifier table) {
        entity.verifyLogicConsistency();
        return Mono.just(entity);
    }

}
