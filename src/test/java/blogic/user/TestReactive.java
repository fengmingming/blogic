package blogic.user;

import blogic.core.security.FuncTrees;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;

public class TestReactive {

    @Test
    public void testEmpty() {
        Flux.just(1,2,3).map(it -> it * 2).switchIfEmpty(Mono.just(10)).subscribe(it -> System.out.println(it));
        Flux.fromIterable(new ArrayList<>()).map(it -> (Integer)it * 2).switchIfEmpty(Mono.just(10)).subscribe(it -> System.out.println(it));
    }

    @Test
    public void testFuncTree() {
        FuncTrees ft = FuncTrees.buildFuncTrees(Arrays.asList("POST:/blogic/login"));
        System.out.println(ft.toString());
    }

    @Test
    public void testDefaultIfEmpty() {
        Flux.empty().doOnNext(it -> System.out.println(it)).thenMany(Flux.just(1))
                .map(it -> it).collectList().doOnNext(it -> System.out.println(it)).subscribe();
    }

    @Test
    public void testThen() {
        Flux.empty().thenMany(Flux.just(1)).doOnNext(it -> System.out.println(it)).subscribe();
    }

    @Test
    public void testNull() {
    }

    @Test
    public void testZip() {
        Mono.zip(Mono.empty(), Mono.just(1)).doOnNext(tuple -> System.out.println(tuple)).subscribe();
    }

    @Test
    public void testConcatMap() {
        System.out.println(Flux.empty().concatWith(Flux.just(1)).collectList().block());
    }

}
