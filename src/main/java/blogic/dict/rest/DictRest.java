package blogic.dict.rest;

import blogic.core.rest.ResVo;
import blogic.dict.domain.repository.DictRepository;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RestController
public class DictRest {

    @Autowired
    private DictRepository dictRepository;

    @GetMapping("/Dict")
    public Mono<ResVo<?>> findAll(@RequestParam(value = "dictType", required = false)String dictType) {
        return Mono.deferContextual(contextView -> {
            Locale locale = contextView.getOrDefault(Locale.class, Locale.getDefault());
            if(StrUtil.isNotBlank(dictType)) {
                return dictRepository.findByDictType(dictType, locale).collectList();
            }
            return dictRepository.findByLocale(locale).collectList();
        }).map(it -> ResVo.success(it));
    }

}
