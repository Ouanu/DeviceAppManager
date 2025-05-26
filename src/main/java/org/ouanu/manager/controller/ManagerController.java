package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.model.User;
import org.ouanu.manager.record.ManagerRegisterRequest;
import org.ouanu.manager.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService service;

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> list() {
        List<User> userList = service.findAllUsers();
        List<UserDto> dtos = userList.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult<Void>> register(
            @Valid @RequestBody ManagerRegisterRequest request
    ) {
        service.register(request);
        return ResponseResult.created();
    }
}
