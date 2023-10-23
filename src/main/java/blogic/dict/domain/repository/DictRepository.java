package blogic.dict.domain.repository;

import blogic.dict.domain.Dict;
import blogic.dict.domain.QDict;
import com.infobip.spring.data.r2dbc.QuerydslR2dbcRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Repository
@Validated
public interface DictRepository extends QuerydslR2dbcRepository<Dict, Long> {

    default Flux<Dict> findByLocale(Locale locale) {
        return query(q -> q.select(QDict.dict)
                .from(QDict.dict)
                .where(QDict.dict.locale.eq(Dict.localeToString(locale)))
        ).all();
    }

    default Flux<Dict> findByDictType(@NotBlank String dictType,@NotNull Locale locale) {
        return query(q -> q.select(QDict.dict)
                .from(QDict.dict)
                .where(QDict.dict.dictType.eq(dictType)
                        .and(QDict.dict.locale.eq(Dict.localeToString(locale))))
        ).all();
    }

    default Mono<Dict> findByDictTypeAndCode(@NotBlank String dictType,@NotNull Integer code) {
        return findByDictTypeAndCodeAndLocale(dictType, code, Locale.getDefault());
    }

    default Mono<Dict> findByDictTypeAndCodeAndLocale(@NotBlank String dictType,@NotNull Integer code,@NotNull Locale locale) {
        return query(q -> q.select(QDict.dict)
                .from(QDict.dict)
                .where(QDict.dict.dictType.eq(dictType)
                        .and(QDict.dict.code.eq(code))
                        .and(QDict.dict.locale.eq(Dict.localeToString(locale))))
        ).one();
    }

}
