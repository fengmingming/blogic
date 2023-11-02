package blogic.productline.infras;

import blogic.core.exception.ForbiddenAccessException;
import blogic.productline.bug.domain.repository.BugRepository;
import blogic.productline.iteration.domain.repository.IterationRepository;
import blogic.productline.product.domain.QProductMember;
import blogic.productline.product.domain.repository.ProductMemberRepository;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.requirement.domain.RequirementRepository;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.testcase.domain.repository.TestCaseRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class ProductLineVerifier {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMemberRepository productMemberRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private IterationRepository iterationRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TestCaseRepository testCaseRepository;
    @Autowired
    private BugRepository bugRepository;

    public Mono<Boolean> verifyProduct(@NotNull Long companyId,@NotNull Long productId) {
        return verify(companyId, productId, null, null, null, null, null);
    }

    public Mono<Boolean> verifyRequirement(@NotNull Long companyId,@NotNull Long productId, Long requirementId) {
        return verify(companyId, productId, requirementId, null, null, null, null);
    }

    public Mono<Boolean> verifyIteration(@NotNull Long companyId,@NotNull Long productId, Long iterationId) {
        return verify(companyId, productId, null, iterationId, null, null, null);
    }

    public Mono<Boolean> verifyTask(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long taskId) {
        return verify(companyId, productId, requirementId, iterationId, taskId, null, null);
    }

    public Mono<Boolean> verifyTestCase(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long testCaseId) {
        return verify(companyId, productId, requirementId, iterationId, null, testCaseId, null);
    }

    public Mono<Boolean> verifyBug(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long testCaseId, Long bugId) {
        return verify(companyId, productId, requirementId, iterationId, null, testCaseId, bugId);
    }

    /**
     * 验证产品是否属于公司
     * 验证产品线下的实体是否属于本产品
     * */
    public Mono<Boolean> verify(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long taskId, Long testCaseId, Long bugId) {
        Objects.requireNonNull(companyId);
        Objects.requireNonNull(productId);
        return Mono.zip(productRepository.verifyProductBelongToCompany(productId, companyId),
                Mono.defer(() -> {
                    if(requirementId == null) {
                        return Mono.just(Boolean.TRUE);
                    }else {
                        return requirementRepository.verifyRequirementBelongToProduct(requirementId, productId);
                    }
                }),
                Mono.defer(() -> {
                    if(iterationId == null) {
                        return Mono.just(Boolean.TRUE);
                    }else {
                        return iterationRepository.verifyIterationBelongToProduct(iterationId, productId);
                    }
                }),
                Mono.defer(() -> {
                    if(taskId == null) {
                        return Mono.just(Boolean.TRUE);
                    }else {
                        return taskRepository.verifyTaskBelongToProduct(taskId, productId);
                    }
                }),
                Mono.defer(() -> {
                    if(testCaseId == null) {
                        return Mono.just(Boolean.TRUE);
                    }else {
                        return testCaseRepository.verifyTestCaseBelongToProduct(testCaseId, productId);
                    }
                }),
                Mono.defer(() -> {
                    if(bugId == null) {
                        return Mono.just(Boolean.TRUE);
                    }else {
                        return bugRepository.verifyBugBelongToProduct(bugId, productId);
                    }
                })
        ).map(tuple6 -> {
            return tuple6.getT1() && tuple6.getT2() && tuple6.getT3() && tuple6.getT4() && tuple6.getT5() && tuple6.getT6();
        });
    }

    public Mono<Void> verifyProductOrThrowException(@NotNull Long companyId,@NotNull Long productId) {
        return verifyOrThrowException(companyId, productId, null, null, null, null, null);
    }

    public Mono<Void> verifyRequirementOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long requirementId) {
        return verifyOrThrowException(companyId, productId, requirementId, null, null, null, null);
    }

    public Mono<Void> verifyIterationOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long iterationId) {
        return verifyOrThrowException(companyId, productId, null, iterationId, null, null, null);
    }

    public Mono<Void> verifyTaskOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long taskId) {
        return verifyOrThrowException(companyId, productId, requirementId, iterationId, taskId, null, null);
    }

    public Mono<Void> verifyTestCaseOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long testCaseId) {
        return verifyOrThrowException(companyId, productId, requirementId, iterationId, null, testCaseId, null);
    }

    public Mono<Void> verifyBugOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long testCaseId, Long bugId) {
        return verifyOrThrowException(companyId, productId, requirementId, iterationId, null, testCaseId, bugId);
    }

    public Mono<Void> verifyOrThrowException(@NotNull Long companyId,@NotNull Long productId, Long requirementId, Long iterationId, Long taskId, Long testCaseId, Long bugId) {
        Mono<Void> verifyMono = productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId);
        if(requirementId != null) {
            verifyMono = verifyMono.then(requirementRepository.verifyRequirementBelongToProductOrThrowException(requirementId, productId));
        }
        if(iterationId != null) {
            verifyMono = verifyMono.then(iterationRepository.verifyIterationBelongToProductOrThrowException(iterationId, productId));
        }
        if(taskId != null) {
            verifyMono = verifyMono.then(taskRepository.verifyTaskBelongToProductOrThrowException(taskId, productId));
        }
        if(testCaseId != null) {
            verifyMono = verifyMono.then(testCaseRepository.verifyTestCaseBelongToProductOrThrowException(testCaseId, productId));
        }
        if(bugId != null) {
            verifyMono = verifyMono.then(bugRepository.verifyBugBelongToProductThrowException(bugId, productId));
        }
        return verifyMono;
    }

    public Mono<Boolean> containsUser(Long productId, Long ... userIds) {
        return productMemberRepository.query(q -> q.select(QProductMember.productMember.id.count())
                .from(QProductMember.productMember)
                .where(QProductMember.productMember.productId.eq(productId).and(QProductMember.productMember.userId.in(userIds)))
        ).one().map(it -> it == userIds.length);
    }

    public Mono<Void> containsUserOrThrowException(Long productId, Long ... userIds) {
        return containsUser(productId, userIds).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
    }

}
