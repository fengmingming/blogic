package blogic.dict.service;

import blogic.dict.domain.Dict;
import blogic.dict.domain.repository.DictRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Service
@Validated
public class DictService {

    @Autowired
    private DictRepository dictRepository;

    public Flux<Dict> findByDictType(@NotBlank String dictType, @NotNull Locale locale) {
        return dictRepository.findByDictType(dictType, locale);
    }

    public Mono<Dict> findOne(@NotBlank String dictType, @NotNull Integer code, @NotNull Locale locale) {
        return dictRepository.findByDictTypeAndCodeAndLocale(dictType, code, locale);
    }

}
