package blogic.core.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public abstract class ActiveRecord<T, ID> {

    protected abstract ReactiveCrudRepository<T, ID> findRepository();

    private <S extends T> S selfS() {
        return selfT();
    }

    protected abstract <T> T selfT();

    public <S extends T> Mono<S> insert() {
        return findRepository().save(selfS());
    }

    public <S extends T> Mono<S> update() {
        return findRepository().save(selfS());
    }

    public Mono<Void> delete() {
        return findRepository().delete(selfS());
    }

}
