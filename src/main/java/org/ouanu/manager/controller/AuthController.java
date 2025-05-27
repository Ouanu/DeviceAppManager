package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.model.User;
import org.ouanu.manager.record.LoginRequest;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.record.TokenResponse;
import org.ouanu.manager.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
        TokenResponse authenticate = authService.authenticate(request.username(), request.password());
        if (Objects.requireNonNull(authenticate.token()).isEmpty()) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Wrong username or password.");
        }
        return ResponseResult.success(
                authenticate
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
