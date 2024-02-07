package blogic.productline.statistic.rest;

import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.bug.domain.QBug;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.iteration.domain.QIteration;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.product.domain.QProduct;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.QRequirement;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.testcase.domain.QTestCase;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import cn.hutool.core.map.MapUtil;
import com.querydsl.core.types.Projections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestController
public class StatisticRest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private BugRepository bugRepository;
    @Autowired
    private TestCaseRepository testCaseRepository;

    @Setter
    @Getter
    public static class StatTuple {
        @Column("status")
        private Integer status;
        @Column("total")
        private Long total;
    }

    /**
     * 公司级别统计
     * */
    @GetMapping("/Companies/{companyId}/Statistic")
    public Mono<ResVo<?>> statisticToCompany(@PathVariable("companyId")Long companyId, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        QProduct qProduct = QProduct.product;
        Mono<Long> productTotalMono = productRepository.query(q -> q.select(qProduct.id.count()).from(qProduct)
                .where(qProduct.deleted.isFalse().and(qProduct.companyId.eq(companyId)))).one();
        QRequirement qRequirement = QRequirement.requirement;
        Flux<StatTuple> requirementFlux = requirementRepository.query(q -> q.select(Projections.bean(StatTuple.class, qRequirement.requirementStatus.as("status"), qRequirement.id.count().as("total")))
                .from(qRequirement).innerJoin(qProduct).on(qRequirement.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qRequirement.deleted.isFalse()).groupBy(qRequirement.requirementStatus)).all();
        QIteration qIteration = QIteration.iteration;
        Flux<StatTuple> iterationFlux = iterationRepository.query(q -> q.select(Projections.bean(StatTuple.class, qIteration.status, qIteration.id.count().as("total")))
                .from(qIteration).innerJoin(qProduct).on(qIteration.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qIteration.deleted.isFalse()).groupBy(qIteration.status)).all();
        QTask qTask = QTask.task;
        Flux<StatTuple> taskFlux = taskRepository.query(q -> q.select(Projections.bean(StatTuple.class, qTask.status, qTask.id.count().as("total")))
                .from(qTask).innerJoin(qProduct).on(qTask.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qTask.deleted.isFalse()).groupBy(qTask.status)).all();
        QBug qBug = QBug.bug;
        Flux<StatTuple> bugFlux = bugRepository.query(q -> q.select(Projections.bean(StatTuple.class, qBug.status, qBug.id.count().as("total")))
                .from(qBug).innerJoin(qProduct).on(qBug.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qBug.deleted.isFalse()).groupBy(qBug.status)).all();
        QTestCase qTC = QTestCase.testCase;
        Flux<StatTuple> tcFlux = testCaseRepository.query(q -> q.select(Projections.bean(StatTuple.class, qTC.status, qTC.id.count().as("total")))
                .from(qTC).innerJoin(qProduct).on(qTC.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qTC.deleted.isFalse()).groupBy(qTC.status)).all();
        return Mono.zip(productTotalMono, requirementFlux.collectList(), iterationFlux.collectList(), taskFlux.collectList(), bugFlux.collectList(), tcFlux.collectList())
                .map(tuple -> {
                    return ResVo.success(MapUtil.builder()
                            .put("proTotal", tuple.getT1())
                            .put("requirementStat", tuple.getT2().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("iterationStat", tuple.getT3().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("taskStat", tuple.getT4().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("bugStat", tuple.getT5().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("tcStat", tuple.getT6().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .build());
                });
    }

    /**
     * 个人统计
     * */
    @GetMapping("/Companies/{companyId}/Users/{userId}/Statistic")
    public Mono<ResVo<?>> statisticToCompany(@PathVariable("companyId")Long companyId, @PathVariable("userId") Long userId,
                                             TokenInfo tokenInfo, UserCurrentContext context) {
        context.equalsCompanyIdOrThrowException(companyId);
        tokenInfo.equalsUserIdOrThrowException(userId);
        QProduct qProduct = QProduct.product;
        Mono<Long> productTotalMono = productRepository.query(q -> q.select(qProduct.id.count()).from(qProduct)
                .where(qProduct.deleted.isFalse().and(qProduct.createUserId.eq(userId)).and(qProduct.companyId.eq(companyId)))).one();
        QRequirement qRequirement = QRequirement.requirement;
        Flux<StatTuple> requirementFlux = requirementRepository.query(q -> q.select(Projections.bean(StatTuple.class, qRequirement.requirementStatus.as("status"), qRequirement.id.count().as("total")))
                .from(qRequirement).innerJoin(qProduct).on(qRequirement.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qRequirement.deleted.isFalse().and(qRequirement.createUserId.eq(userId))).groupBy(qRequirement.requirementStatus)).all();
        QIteration qIteration = QIteration.iteration;
        Flux<StatTuple> iterationFlux = iterationRepository.query(q -> q.select(Projections.bean(StatTuple.class, qIteration.status, qIteration.id.count().as("total")))
                .from(qIteration).innerJoin(qProduct).on(qIteration.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qIteration.deleted.isFalse().and(qIteration.createUserId.eq(userId))).groupBy(qIteration.status)).all();
        QTask qTask = QTask.task;
        Flux<StatTuple> taskFlux = taskRepository.query(q -> q.select(Projections.bean(StatTuple.class, qTask.status, qTask.id.count().as("total")))
                .from(qTask).innerJoin(qProduct).on(qTask.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qTask.deleted.isFalse().and(qTask.createUserId.eq(userId).or(qTask.currentUserId.eq(userId)))).groupBy(qTask.status)).all();
        QBug qBug = QBug.bug;
        Flux<StatTuple> bugFlux = bugRepository.query(q -> q.select(Projections.bean(StatTuple.class, qBug.status, qBug.id.count().as("total")))
                .from(qBug).innerJoin(qProduct).on(qBug.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qBug.deleted.isFalse().and(qBug.createUserId.eq(userId).or(qBug.currentUserId.eq(userId)))).groupBy(qBug.status)).all();
        QTestCase qTC = QTestCase.testCase;
        Flux<StatTuple> tcFlux = testCaseRepository.query(q -> q.select(Projections.bean(StatTuple.class, qTC.status, qTC.id.count().as("total")))
                .from(qTC).innerJoin(qProduct).on(qTC.productId.eq(qProduct.id).and(qProduct.companyId.eq(companyId)).and(qProduct.deleted.isFalse()))
                .where(qTC.deleted.isFalse().and(qTC.createUserId.eq(userId).or(qTC.ownerUserId.eq(userId)))).groupBy(qTC.status)).all();
        return Mono.zip(productTotalMono, requirementFlux.collectList(), iterationFlux.collectList(), taskFlux.collectList(), bugFlux.collectList(), tcFlux.collectList())
                .map(tuple -> {
                    return ResVo.success(MapUtil.builder()
                            .put("proTotal", tuple.getT1())
                            .put("requirementStat", tuple.getT2().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("iterationStat", tuple.getT3().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("taskStat", tuple.getT4().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("bugStat", tuple.getT5().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .put("tcStat", tuple.getT6().stream().collect(Collectors.toMap(StatTuple::getStatus, StatTuple::getTotal)))
                            .build());
                });
    }

}