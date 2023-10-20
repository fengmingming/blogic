package blogic.productline.task.rest;

import blogic.core.rest.Paging;
import blogic.core.rest.ResVo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.domain.repository.ProductRepository;
import blogic.productline.task.domain.QTask;
import blogic.productline.task.domain.TaskStatusEnum;
import blogic.productline.task.domain.repository.TaskRepository;
import blogic.productline.task.service.TaskService;
import blogic.user.domain.QUser;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class TaskRest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProductRepository productRepository;

    @Initialized

    @Setter
    @Getter
    public static class FindTasksReq extends Paging {
        private String taskName;
        private Long createUserId;
        private Long currentUserId;
        private Long completeUserId;
        private TaskStatusEnum taskStatus;
    }

    @Setter
    @Getter
    public static class FindTasksRes {
        private Long id;
        private Long iterationId;
        private String iterationName;
        private Long productId;
        private String taskName;
        private Integer status;
        private Long currentUserId;
        @Column("currentUserName")
        private String currentUserName;
        private Long completeUserId;
        @Column("completeUserName")
        private String completeUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completeTime;
        private Integer allTime;
        private Integer consumeTime;
        private String createUserId;
        @Column("createUserName")
        private String createUserName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    @GetMapping("/Companies/{companyId}/Products/{productId}/Tasks")
    public Mono<ResVo<?>> findTasks(@PathVariable("companyId")Long companyId, @PathVariable("productId")Long productId,
                                    UserCurrentContext context, FindTasksReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        Mono<Void> verifyProductIdMono = productRepository.verifyProductBelongToCompanyOrThrowException(productId, companyId);
        Mono<ResVo<?>> resVoMono = taskRepository.query(q -> {
            QTask qTask = QTask.task;
            QUser currentQUser = QUser.user;
            QUser completeQUser = QUser.user;
            QUser createQUser = QUser.user;
            QBean<FindTasksRes> qBean = Projections.bean(FindTasksRes.class, qTask, currentQUser.name.as("currentUserName"), completeQUser.name.as("completeUserName"), createQUser.name.as("createUserName"));
            Predicate predicate = qTask.productId.eq(productId).and(qTask.deleted.eq(false));
            if(StrUtil.isNotBlank(req.getTaskName())) {
                predicate = ExpressionUtils.and(predicate, qTask.taskName.like(req.getTaskName()));
            }
            if(req.getTaskStatus() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.status.eq(req.getTaskStatus().getCode()));
            }
            if(req.getCurrentUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.currentUserId.eq(req.getCurrentUserId()));
            }
            if(req.getCompleteUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.completeUserId.eq(req.getCompleteUserId()));
            }
            if(req.getCreateUserId() != null) {
                predicate = ExpressionUtils.and(predicate, qTask.createUserId.eq(req.getCreateUserId()));
            }
            return q.select(qBean)
                    .from(qTask)
                    .leftJoin(currentQUser).on(qTask.currentUserId.eq(currentQUser.id))
                    .leftJoin(completeQUser).on(qTask.completeUserId.eq(completeQUser.id))
                    .leftJoin(createQUser).on(qTask.createUserId.eq(createQUser.id))
                    .where(predicate)
                    .orderBy(qTask.createTime.desc())
                    .offset(req.getOffset()).limit(req.getLimit());
        }).all().collectList().map(it -> ResVo.success(it));
        return verifyProductIdMono.then(resVoMono);
    }

}
