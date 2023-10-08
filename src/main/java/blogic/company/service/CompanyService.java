package blogic.company.service;

import blogic.company.domain.Company;
import blogic.company.domain.Department;
import blogic.company.domain.repository.CompanyRepository;
import blogic.company.domain.repository.DepartmentRepository;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.UserCompanyRole;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Validated
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;

    @Setter
    @Getter
    public static class CreateCompanyCommand {
        @NotNull
        private Long userId;
        @NotBlank
        @Length(max = 254)
        private String companyName;
    }

    @Transactional
    public Mono<Long> createCompany(@Valid CreateCompanyCommand command) {
        Company company = new Company();
        company.setCompanyName(command.getCompanyName());
        company.setCreateTime(LocalDateTime.now());
        return companyRepository.save(company).flatMap(it -> {
            UserCompanyRole ucr = new UserCompanyRole();
            ucr.setCompanyId(it.getId());
            ucr.setUserId(command.getUserId());
            ucr.setRole(RoleEnum.ROLE_MANAGER);
            ucr.setAdmin(true);
            return userCompanyRoleRepository.save(ucr);
        }).flatMap(it -> Mono.just(it.getCompanyId()));
    }

    @Setter
    @Getter
    public static class UpdateCompanyCommand {
        @NotBlank
        @Length(max = 254)
        private String companyName;
    }

    @Transactional
    public Mono<Void> updateCompany(@Valid UpdateCompanyCommand command, @NotNull Company company) {
        company.setCompanyName(command.getCompanyName());
        company.setUpdateTime(LocalDateTime.now());
        return companyRepository.save(company).then(Mono.empty());
    }

    @Setter
    @Getter
    public static class CreateDepartmentCommand {
        @NotNull
        private Long companyId;
        @NotBlank
        @Length(max = 254)
        private String departmentName;
        @NotNull
        private Long parentId;
    }

    @Transactional
    public Mono<Long> createDepartment(@Valid CreateDepartmentCommand command) {
        Department department = new Department();
        department.setDepartmentName(command.getDepartmentName());
        department.setCompanyId(command.getCompanyId());
        department.setParentId(command.getParentId());
        department.setCreateTime(LocalDateTime.now());
        return departmentRepository.save(department).map(it -> it.getId());
    }

    @Setter
    @Getter
    public static class UpdateDepartmentCommand {
        @NotBlank
        @Length(max = 254)
        private String departmentName;
        private Long parentId;
    }

    @Transactional
    public Mono<Void> updateDepartment(@Valid @NotNull UpdateDepartmentCommand command,@NotNull Department department) {
        department.setUpdateTime(LocalDateTime.now());
        department.setDepartmentName(command.getDepartmentName());
        department.setParentId(command.getParentId());
        return departmentRepository.save(department).then(Mono.empty());
    }

    @Transactional
    public Mono<Void> bindUserToDepartment(Long userId, Long departmentId) {
        return departmentRepository.existUser(departmentId, userId).flatMap(it -> {
            if(it) {
                return Mono.empty();
            }else {
                return departmentRepository.bindUser(departmentId, userId);
            }
        });
    }

}
