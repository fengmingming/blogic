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

}
