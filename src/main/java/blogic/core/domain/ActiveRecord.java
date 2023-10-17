package blogic.core.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public abstract class ActiveRecord<T, ID> {

    protected abstract ReactiveCrudRepository<T, ID> findRepository();

    protected abstract <S extends T> S selfS();

    public <S extends T> Mono<S> save() {
        return findRepository().save(selfS());
    }

    public Mono<Void> delete() {
        return findRepository().delete(selfS());
    }

}
