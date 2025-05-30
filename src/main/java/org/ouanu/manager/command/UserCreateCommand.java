package org.ouanu.manager.command;

import org.ouanu.manager.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

// 用于创建用户的类
public record UserCreateCommand(
        String username,
        String email,
        String phone,
        String password,
        String remark// 仅限临时存储
) {

    @Override
    public String username() {
        return username;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public String phone() {
        return phone;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String remark() {
        return remark;
    }

    public User toEntity(PasswordEncoder encoder) {
        return User.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .password(encoder.encode(password))
                .remark(remark)
                .build();
    }
}
