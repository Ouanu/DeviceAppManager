package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.response.UserResponse;
import org.ouanu.manager.request.RegisterUserRequest;
import org.ouanu.manager.response.TokenResponse;
import org.ouanu.manager.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 服务层: 处理核心业务逻辑
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    public TokenResponse authenticate(String username, String password) {
        if (!userService.validateCredentials(username, password)) {
//            throw new RuntimeException("用户名或密码错误");
            return new TokenResponse("");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(
                authenticationToken
        );
        userService.loginUpdateLastModifiedTime(username);
        return new TokenResponse(jwtUtils.generateJwtToken(auth));
    }

    public void register(RegisterUserRequest request) {
        userService.createUser(request.toCommand());
    }

    public UserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        UserDto user = userService.loadUserDtoByUsername(userDetails.getUsername());
        if (user == null) {
            System.out.println("Auth Service = null");
            return null;
        }
        System.out.println("Auth Service = " + user);
        return UserResponse.fromEntity(user);
    }
}
