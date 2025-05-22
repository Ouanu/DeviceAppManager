package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.record.TokenResponse;
import org.ouanu.manager.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        return new TokenResponse(jwtUtils.generateJwtToken(auth));
    }

    public void register(RegisterRequest request) {
        userService.createUser(request.toCommand());
    }
}
