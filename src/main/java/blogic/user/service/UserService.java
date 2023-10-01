package blogic.user.service;

import blogic.company.domain.Company;
import blogic.core.security.AuthenticateFilter;
import blogic.core.security.JwtTokenUtil;
import blogic.core.security.TerminalTypeEnum;
import blogic.user.domain.RoleEnum;
import blogic.user.domain.User;
import blogic.user.domain.UserCompanyRole;
import blogic.user.domain.repository.CompanyRepository;
import blogic.user.domain.repository.UserCompanyRoleRepository;
import blogic.user.domain.repository.UserRepository;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Validated
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCompanyRoleRepository userCompanyRoleRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AuthenticateFilter.JwtKeyProperties jwtKeyProperties;

    @Transactional
    public Mono<User> createUser(@Valid User user) {
        if(StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        user.setUpdateTime(user.getCreateTime());

        Company company = new Company();
        company.setCompanyName(user.getName());
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());

        return Mono.zip(userRepository.save(user), companyRepository.save(company))
            .flatMap(tuple -> {
                UserCompanyRole ucr = new UserCompanyRole();
                ucr.setAdmin(true);
                ucr.setUserId(tuple.getT1().getId());
                ucr.setRole(RoleEnum.ROLE_MANAGER);
                ucr.setCompanyId(tuple.getT2().getId());
                return userCompanyRoleRepository.save(ucr).then(Mono.just(tuple.getT1()));
            });
    }

    @Setter
    @Getter
    public static class UpdateUserCommand {
        @NotNull
        private Long userId;
        @Length(max = 100)
        private String name;
    }

    @Transactional
    public Mono<User> updateUser(@Valid UpdateUserCommand com) {
        return userRepository.findById(com.getUserId()).doOnNext(user -> {
            user.setName(com.getName());
        }).flatMap(user -> userRepository.save(user));
    }

    public Mono<String> createToken(Long userId, TerminalTypeEnum terminal) {
        return userRepository.findById(userId).map(user -> JwtTokenUtil.generateToken(user.getId(),
                terminal, jwtKeyProperties.getKey().getBytes(StandardCharsets.UTF_8)));
    }

    public Flux<UserCompanyDto> findUserCompaniesByUserId(Long userId) {
        return userCompanyRoleRepository.findByUserId(userId).collectList().map(ucrs -> {
            return ucrs.stream().collect(Collectors.groupingBy(UserCompanyRole::getCompanyId));
        }).flatMapIterable(group -> group.entrySet()).flatMap(entry -> companyRepository.findById(entry.getKey())
                .filter(it -> !it.getDeleted()).map(company -> {
            UserCompanyDto userCompanyDto = new UserCompanyDto();
            userCompanyDto.setCompanyId(company.getId());
            userCompanyDto.setUserId(userId);
            userCompanyDto.setCompanyName(company.getCompanyName());
            userCompanyDto.setAdmin(entry.getValue().stream().filter(it -> it.getAdmin()).findFirst().isPresent());
            userCompanyDto.setRoles(entry.getValue().stream().map(it -> it.getRole()).collect(Collectors.toList()));
            return userCompanyDto;
        }));
    }

    /**
     * 分配角色
     * @param userId 用户
     * @param companyId 公司
     * @param roles 角色列表
     * */
    @Transactional
    public Mono<Void> assignRoles(Long userId, Long companyId, List<RoleEnum> roles) {
        return userCompanyRoleRepository.findByUserId(userId).filter(ucr -> ucr.getCompanyId().equals(companyId)).collectList()
            .flatMap(ucrs -> {
                List<RoleEnum> existRoles = ucrs.stream().map(it -> it.getRole()).collect(Collectors.toList());
                Collection<RoleEnum> newRoles = CollectionUtil.subtract(roles, existRoles);
                Collection<RoleEnum> deleteRoles = CollectionUtil.subtract(existRoles, roles);
                List<Long> deleteIds = ucrs.stream().filter(it -> deleteRoles.contains(it.getRole())).map(it -> it.getId()).collect(Collectors.toList());
                return userCompanyRoleRepository.deleteById(Flux.fromStream(deleteIds.stream()))
                    .then(userCompanyRoleRepository.saveAll(Flux.fromStream(newRoles.stream().map(it -> {
                        UserCompanyRole ucr = new UserCompanyRole();
                        ucr.setRole(it);
                        ucr.setUserId(userId);
                        ucr.setCompanyId(companyId);
                        return ucr;
                    }))).collectList()).then();
            });
    }

}
