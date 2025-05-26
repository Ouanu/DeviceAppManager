package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.model.User;
import org.ouanu.manager.record.ManagerRegisterRequest;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ManagerService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
    }

    public void register(ManagerRegisterRequest request) {
        createUser(request.toCommand());
    }

    @Transactional
    public void createUser(ManagerCreateCommand command) {
        if (userRepository.existsByUsername(command.username)) {
            throw new ConflictException("用户名已存在");
        }
        if (userRepository.existsByEmail(command.email)) {
            throw new ConflictException("Email已存在");
        }
        if (userRepository.existsByPhone(command.phone)) {
            throw new ConflictException("手机号已存在");
        }

        User user = command.toEntity(passwordEncoder);
        user.setUuid(UUID.randomUUID().toString());
        user.setCreateTime(LocalDateTime.now());
        user.setExpireDate(LocalDateTime.of(2095, 1, 1, 0, 0));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setLastModifiedTime(LocalDateTime.now());
        user.setRole("ADMIN");
        userRepository.save(user);

        if (!userRepository.existsByUsername(command.username)) {
            throw new ConflictException("用户名创建失败");
        }
    }

    public record ManagerCreateCommand(
            String username,
            String email,
            String phone,
            String password // 仅限临时存储
    ) {
        public User toEntity(PasswordEncoder encoder) {
            return User.builder()
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .password(encoder.encode(password))
                    .build();
        }
    }
}
