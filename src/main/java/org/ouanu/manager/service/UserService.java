package org.ouanu.manager.service;

import org.ouanu.manager.dto.AuthRequest;
import org.ouanu.manager.dto.AuthResponse;
import org.ouanu.manager.dto.RegisterRequest;
import org.ouanu.manager.dto.UserDTO;
import org.ouanu.manager.exception.AppException;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.UserRepository;
import org.ouanu.manager.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AppException("用户名已存在", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new AppException("手机号已被注册", HttpStatus.BAD_REQUEST);
        }

        User user = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getPhoneNumber()
        );
        userRepository.save(user);
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        String username = authRequest.getUsernameOrPhoneNumber();
        System.out.println("in authenticate: " + username);
        if (userRepository.existsByPhoneNumber(authRequest.getUsernameOrPhoneNumber())) {
            username = userRepository.findUsernameByPhoneNumber(username);
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        authRequest.getPassword()
                )
        );
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));

        String jwt = jwtService.generateToken(user.getUsername());
        return new AuthResponse(jwt, user.getUsername(), user.getUuid());
    }

    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));

        return new UserDTO(
                user.getUsername(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUuid()
                );
    }
}