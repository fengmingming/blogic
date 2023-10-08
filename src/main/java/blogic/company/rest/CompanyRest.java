package blogic.company.rest;

import blogic.company.domain.Department;
import blogic.company.domain.repository.CompanyRepository;
import blogic.company.domain.repository.DepartmentRepository;
import blogic.company.service.CompanyService;
import blogic.core.exception.DataNotFoundException;
import blogic.core.exception.ForbiddenAccessException;
import blogic.core.rest.ResVo;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.User;
import blogic.user.domain.repository.UserRepository;
import blogic.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class CompanyRest {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserService userService;

    @Setter
    @Getter
    public static class CreateCompanyReq {
        @NotBlank
        @Length(max = 254)
        private String companyName;
    }

    @PostMapping("/Companies")
    public Mono<ResVo<?>> createCompany(TokenInfo tokenInfo, @RequestBody @Valid CreateCompanyReq req) {
        CompanyService.CreateCompanyCommand command = new CompanyService.CreateCompanyCommand();
        command.setUserId(tokenInfo.getUserId());
        command.setCompanyName(req.getCompanyName());
        return companyService.createCompany(command).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class UpdateCompanyReq {
        @NotBlank
        @Length(max = 254)
        private String companyName;
    }

    @PutMapping("/Companies/{companyId}")
    public Mono<ResVo<?>> updateCompany(@PathVariable("companyId")Long companyId, UserCurrentContext context,
                                        @RequestBody @Valid UpdateCompanyReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_MANAGER);
        return companyRepository.findById(companyId).switchIfEmpty(Mono.error(new DataNotFoundException()))
                .flatMap(company -> {
                    CompanyService.UpdateCompanyCommand command = new CompanyService.UpdateCompanyCommand();
                    command.setCompanyName(req.getCompanyName());
                    return companyService.updateCompany(command, company);
                }).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class CreateDepartmentReq {
        @NotBlank
        @Length(max = 254)
        private String departmentName;
        private Long parentId;
    }

    @PostMapping("/Companies/{companyId}/Departments")
    public Mono<ResVo<?>> createDepartment(@PathVariable("companyId")Long companyId,
                                           UserCurrentContext context, @RequestBody @Valid CreateDepartmentReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_MANAGER);
        Mono<Long> createDepartmentMono = Mono.fromSupplier(() -> {
            CompanyService.CreateDepartmentCommand command = new CompanyService.CreateDepartmentCommand();
            command.setDepartmentName(req.getDepartmentName());
            command.setCompanyId(companyId);
            command.setParentId(req.getParentId());
            return command;
        }).flatMap(command -> companyService.createDepartment(command));

        if(req.getParentId() != null) {
            return departmentRepository.findById(req.getParentId())
                    .switchIfEmpty(Mono.error(new DataNotFoundException()))
                    .flatMap(it -> {
                        if(!it.getCompanyId().equals(companyId)){
                            return Mono.error(new ForbiddenAccessException());
                        }else {
                            return createDepartmentMono;
                        }
                    }).then(Mono.just(ResVo.success()));
        }else {
            return createDepartmentMono.then(Mono.just(ResVo.success()));
        }
    }

    @Setter
    @Getter
    public static class UpdateDepartmentReq {
        @NotBlank
        @Length(max = 254)
        private String departmentName;
        private Long parentId;
    }

    @PutMapping("/Companies/{companyId}/Departments/{departmentId}")
    public Mono<ResVo<?>> updateDepartment(@PathVariable("companyId") Long companyId, @PathVariable("departmentId") Long departmentId,
                                           UserCurrentContext context, @RequestBody @Valid UpdateDepartmentReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_MANAGER);
        return departmentRepository.findById(departmentId)
                .switchIfEmpty(Mono.error(new DataNotFoundException()))
                .flatMap(it -> {
                    if(it.getCompanyId().equals(companyId)) {
                        CompanyService.UpdateDepartmentCommand command = new CompanyService.UpdateDepartmentCommand();
                        command.setDepartmentName(req.getDepartmentName());
                        command.setParentId(req.getParentId());
                        return companyService.updateDepartment(command, it);
                    }else {
                        return Mono.error(new ForbiddenAccessException());
                    }
                }).then(Mono.just(ResVo.success()));
    }

    @Setter
    @Getter
    public static class BindUserReq {
        @NotNull
        private Long userId;
    }

    @PutMapping("/Companies/{companyId}/Departments/{departmentId}")
    public Mono<ResVo<?>> bindUser(@PathVariable("companyId") Long companyId, @PathVariable("departmentId") Long departmentId,
                                   UserCurrentContext context, @RequestBody @Valid BindUserReq req) {
        context.equalsCompanyIdOrThrowException(companyId);
        context.authenticateOrThrowException(RoleEnum.ROLE_MANAGER);
        Mono<Department> validDepartmentMono = departmentRepository.findById(departmentId)
                .switchIfEmpty(Mono.error(() -> new DataNotFoundException()))
                .flatMap(it -> {
                    if(it.getCompanyId().equals(companyId)) {
                        return Mono.just(it);
                    }else {
                        return Mono.error(new ForbiddenAccessException());
                    }
                });
        Mono<Void> validUserMono = userService.validUserIdAndCompanyId(req.getUserId(), companyId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return Mono.error(new ForbiddenAccessException());
            }
        });
        return validDepartmentMono.then(validUserMono).then(companyService.bindUserToDepartment(req.getUserId(), departmentId))
                .then(Mono.just(ResVo.success()));
    }

}
