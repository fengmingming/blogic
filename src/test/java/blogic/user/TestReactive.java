package blogic.user;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public class TestReactive {

    @Test
    public void testEmpty() {
        Flux.just(1,2,3).map(it -> it * 2).switchIfEmpty(Mono.just(10)).subscribe(it -> System.out.println(it));
        Flux.fromIterable(new ArrayList<>()).map(it -> (Integer)it * 2).switchIfEmpty(Mono.just(10)).subscribe(it -> System.out.println(it));
    }

}
