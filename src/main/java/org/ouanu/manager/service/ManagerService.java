package org.ouanu.manager.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.command.ManagerCreateCommand;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.iface.PermissionCheck;
import org.ouanu.manager.model.User;
import org.ouanu.manager.query.UserQuery;
import org.ouanu.manager.repository.UserRepository;
import org.ouanu.manager.request.DeleteUserOrManagerRequest;
import org.ouanu.manager.request.RegisterManagerRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;


    public List<UserDto> findByConditions(UserQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(query.getUsername())) {
            predicates.add(cb.like(root.get("username"), "%" + query.getUsername() + "%"));
        }
        if (StringUtils.hasText(query.getEmail())) {
            predicates.add(cb.like(root.get("email"), "%" + query.getEmail() + "%"));
        }
        if (StringUtils.hasText(query.getPhone())) {
            predicates.add(cb.like(root.get("phone"), "%" + query.getPhone() + "%"));
        }
        if (StringUtils.hasText(query.getRole())) {
            predicates.add(cb.equal(root.get("role"), query.getRole()));
        }
        if (query.getActive() != null) {
            predicates.add(cb.equal(root.get("active"), query.getActive()));
        }
        if (query.getLocked() != null) {
            predicates.add(cb.equal(root.get("locked"), query.getLocked()));
        }
        if (query.getCreateTimeRange() != null) {
            if (query.getCreateTimeRange().getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createTime"), query.getCreateTimeRange().getStart()));
            }
            if (query.getCreateTimeRange().getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createTime"), query.getCreateTimeRange().getEnd()));
            }
        }
        if (query.getLastModifiedTimeRange() != null) {
            if (query.getLastModifiedTimeRange().getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("lastModifiedTime"), query.getLastModifiedTimeRange().getStart()));
            }
            if (query.getLastModifiedTimeRange().getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("lastModifiedTime"), query.getLastModifiedTimeRange().getEnd()));
            }
        }
        cq.where(predicates.toArray(new Predicate[0]));
        List<User> userList = entityManager.createQuery(cq).getResultList();
        List<UserDto> list = new ArrayList<>();
        for (User user : userList) {
            UserDto dto = UserDto.fromEntity(user);
            list.add(dto);
        }
        return list;
    }

    public void register(RegisterManagerRequest request) {
        createUser(request.toCommand());
    }

    @Transactional
    public boolean delete(DeleteUserOrManagerRequest request) {
        return userRepository.deleteByUuid(request.uuid()) > 0;
    }

    @Transactional
    public boolean hardDelete(DeleteUserOrManagerRequest request) {
        return userRepository.hardDeleteByUuid(request.uuid()) > 0;
    }

    @Transactional
    public void createUser(ManagerCreateCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new ConflictException("用户名已存在");
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new ConflictException("Email已存在");
        }
        if (userRepository.existsByPhone(command.phone())) {
            throw new ConflictException("手机号已存在");
        }

        User user = command.toEntity(passwordEncoder);
        user.setUuid(UUID.randomUUID().toString());
        user.setCreateTime(LocalDateTime.now());
        user.setExpireDate(LocalDateTime.of(2095, 1, 1, 0, 0));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setLastModifiedTime(LocalDateTime.now());
        user.setRole("ADMIN");
        user.setRemark(command.remark());
        userRepository.save(user);

        if (!userRepository.existsByUsername(command.username())) {
            throw new ConflictException("用户名创建失败");
        }
    }
}
