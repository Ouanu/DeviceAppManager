package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// 领域服务层
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(UserCreateCommand command) {
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
        userRepository.save(user);

        if (!userRepository.existsByUsername(command.username)) {
            throw new ConflictException("用户名创建失败");
        }
    }

    public UserDto loadUserDtoByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        UserDto dto = new UserDto();
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRemark(user.getRemark());
        dto.setCreateTime(user.getCreateTime().toString());
        return dto;
    }

    public record ErrorResponse(
            String code,        // 业务错误码
            String message,     // 用户可读信息
            Map<String, Object> details // 额外数据
    ) {
    }

    // 领域命令对象（内部传递）
    public record UserCreateCommand(
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

