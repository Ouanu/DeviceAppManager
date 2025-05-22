package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.record.LoginRequest;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.record.TokenResponse;
import org.ouanu.manager.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

//    @RateLimiter(value = 3, key = "#request.ip")
    @PostMapping("/login")
    public ResponseEntity<ResponseResult<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseResult.success(
                authService.authenticate(request.username(), request.password())
        );
    }

    @PostMapping("/register")
//    @Idempotent(key = "#request.email", expire = 30, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<ResponseResult<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseResult.created();
    }

}
