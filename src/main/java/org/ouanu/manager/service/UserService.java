package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.command.UserCreateCommand;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

// 领域服务层
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(UserCreateCommand command) {
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
        user.setRemark(command.remark());
        userRepository.save(user);

        if (!userRepository.existsByUsername(command.username())) {
            throw new ConflictException("用户创建失败");
        }
    }

    @Transactional
    public void loginUpdateLastModifiedTime(String username) {
        userRepository.updateLoginTime(username, LocalDateTime.now());
    }

    // Verify username and password.
    public boolean validateCredentials(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User is not exists: " + username));
    }
}

