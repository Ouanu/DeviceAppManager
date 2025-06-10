package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.common.UserAuthenticationToken;
import org.ouanu.manager.model.User;
import org.ouanu.manager.response.UserResponse;
import org.ouanu.manager.request.RegisterUserRequest;
import org.ouanu.manager.response.UserTokenResponse;
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
    private final EnhancedJwtService jwtService;

    public UserTokenResponse authenticate(String username, String password) {
        if (!userService.validateCredentials(username, password)) {
            return new UserTokenResponse("");
        }
        User user = userService.loadUserByUsername(username);
        UserAuthenticationToken auth = new UserAuthenticationToken(user, null);
        userService.loginUpdateLastModifiedTime(username);
        return new UserTokenResponse(jwtService.generateToken(auth));
    }

    public void register(RegisterUserRequest request) {
        userService.createUser(request.toCommand());
    }

    public UserResponse getUserInfo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userService.loadUserByUsername(userDetails.getUsername());
            if (userService.validateCredentials(userDetails.getUsername(), userDetails.getPassword())) {
                return UserResponse.fromEntity(user);
            }
            return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
}
