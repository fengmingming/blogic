package blogic.productline.model.rest;

import blogic.core.rest.PagedVo;
import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.productline.infras.ProductLineVerifier;
import blogic.productline.model.domain.Model;
import blogic.productline.model.domain.QModel;
import blogic.productline.model.domain.repository.ModelRepository;
import cn.hutool.core.util.StrUtil;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ModelRest {

    @Resource
    private ModelRepository modelRepository;
    @Resource
    private ProductLineVerifier productLineVerifier;

    @GetMapping("/Companies/{companyId}/Products/{productId}/Models")
    public Mono<ResVo<PagedVo<Model>>> findList(@PathVariable("companyId")Long companyId, @PathVariable("productId") Long productId,
                                             @RequestParam(value = "name", required = false) String name, Paging paging) {
        productLineVerifier.verifyProductOrThrowException(companyId, productId);
        QModel qModel = QModel.model;
        Predicate predicate = qModel.productId.eq(productId);
        if(StrUtil.isNotBlank(name)) {
            predicate = ExpressionUtils.and(predicate, qModel.name.like("%" + name + "%"));
        }
        Predicate predicateFinal = predicate;
        Mono<List<Model>> dataMono = modelRepository.query(q -> {
           return q.select(qModel)
                   .from(qModel)
                   .where(predicateFinal)
                   .orderBy(qModel.updateTime.desc())
                   .offset(paging.getOffset()).limit(paging.getLimit());
        }).all().collectList();
        Mono<Long> totalMono = modelRepository.query(q -> {
            return q.select(qModel.id.count())
                    .from(qModel)
                    .where(predicateFinal);
        }).one();
        return Mono.zip(dataMono, totalMono).map(tuple2 -> ResVo.success(tuple2.getT2(), tuple2.getT1()));
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Models/{modelId}")
    public Mono<ResVo<Model>> findOne(@PathVariable("companyId")Long companyId, @PathVariable("productId") Long productId,
                                      @PathVariable("modelId") Long modelId) {
        productLineVerifier.verifyProductOrThrowException(companyId, productId);
        QModel qModel = QModel.model;
        Mono<Model> modelMono = modelRepository.query(q -> q.select(qModel).from(qModel)
                .where(qModel.id.eq(modelId).and(qModel.productId.eq(productId)))).one();
        return modelMono.map(it -> ResVo.success(it));
    }

    @Setter
    @Getter
    public static class ModelReq {
        @NotBlank(message = "name is blank")
        @Length(max = 254)
        private String name;
        @NotBlank(message = "data is blank")
        private String data;
    }

    @PostMapping("/Companies/{companyId}/Products/{productId}/Models")
    public Mono<ResVo<?>> addModel(@PathVariable("companyId")Long companyId, @PathVariable("productId") Long productId, @Valid @RequestBody ModelReq req) {
        productLineVerifier.verifyProductOrThrowException(companyId, productId);
        Model model = new Model();
        model.setProductId(productId);
        model.setName(req.getName());
        model.setData(req.getData());
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(model.getCreateTime());
        return modelRepository.save(model).then(Mono.just(ResVo.success()));
    }

    @PutMapping("/Companies/{companyId}/Products/{productId}/Models/{modelId}")
    public Mono<ResVo<?>> addModel(@PathVariable("companyId")Long companyId, @PathVariable("productId") Long productId,
                                   @PathVariable("modelId") Long modelId, @Valid @RequestBody ModelReq req) {
        productLineVerifier.verifyProductOrThrowException(companyId, productId);
        QModel qModel = QModel.model;
        Mono<Model> modelMono = modelRepository.query(q -> q.select(qModel).from(qModel)
                .where(qModel.id.eq(modelId).and(qModel.productId.eq(productId)))).one();
        return modelMono.doOnNext(it -> {
            it.setName(req.getName());
            it.setData(req.getData());
            it.setUpdateTime(LocalDateTime.now());
        }).flatMap(it -> modelRepository.save(it)).then(Mono.just(ResVo.success()));
    }

}