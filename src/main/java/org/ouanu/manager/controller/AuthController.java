package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.iface.PermissionCheck;
import org.ouanu.manager.response.UserResponse;
import org.ouanu.manager.request.LoginRequest;
import org.ouanu.manager.request.RegisterUserRequest;
import org.ouanu.manager.response.UserTokenResponse;
import org.ouanu.manager.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //    @RateLimiter(value = 3, key = "#request.ip")
    @PostMapping("/login")
    public ResponseEntity<ResponseResult<UserTokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserTokenResponse authenticate = authService.authenticate(request.username(), request.password());
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
            @Valid @RequestBody RegisterUserRequest request
    ) {
        authService.register(request);
        return ResponseResult.created();
    }

    @PermissionCheck(roles = {"CUSTOMER", "ADMIN"})
    @GetMapping("/me")
    public ResponseEntity<ResponseResult<UserResponse>> me() {
        UserResponse me = authService.getUserInfo();
        System.out.println(me);
        if (me == null) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "User wasn't exists---------");
        } else {
            System.out.println("------------------" + me);
            return ResponseResult.success(me);
        }
    }

}
