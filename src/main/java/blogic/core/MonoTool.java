package blogic.core;

import blogic.core.exception.DataChangedException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class MonoTool {

    public static Function<Long, Mono<Void>> handleUpdateResult(long l) {
        return it -> {
            if(it == l) {
                return Mono.empty();
            }else {
                return Mono.error(new DataChangedException());
            }
        };
    }

    public static Function<Long, Mono<Void>> handleUpdateResult() {
        return handleUpdateResult(1);
    }

}
