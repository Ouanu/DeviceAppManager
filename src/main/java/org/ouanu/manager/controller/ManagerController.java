package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.model.User;
import org.ouanu.manager.query.UserQuery;
import org.ouanu.manager.record.ManagerRegisterRequest;
import org.ouanu.manager.service.ManagerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService service;

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> listUsers(
            @RequestParam Map<String, String> params
    ) {
        UserQuery.UserQueryBuilder query = UserQuery.builder();
        if (params.containsKey("username"))
            query.username(params.get("username"));
        if (params.containsKey("email"))
            query.email(params.get("email"));
        if (params.containsKey("phone"))
            query.phone(params.get("phone"));
        if (params.containsKey("role"))
            query.role(params.get("role"));
        if (params.containsKey("active"))
            query.active(params.get("active").equals("true"));
        if (params.containsKey("locked"))
            query.locked(params.get("locked").equals("true"));
        if (params.containsKey("startCreateTime") && params.containsKey("endCreateTime"))
            query.createTimeRange(new UserQuery.TimeRange(LocalDateTime.parse(params.get("startTime")), LocalDateTime.parse(params.get("endTime"))));
        if (params.containsKey("startModifiedTime") && params.containsKey("endModifiedTime"))
            query.lastModifiedTimeRange(new UserQuery.TimeRange(LocalDateTime.parse(params.get("startModifiedTime")), LocalDateTime.parse(params.get("endModifiedTime"))));
        return ResponseEntity.ok(service.findByConditions(query.build()));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult<Void>> register(
            @Valid @RequestBody ManagerRegisterRequest request
    ) {
        service.register(request);
        return ResponseResult.created();
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseResult<Void>> delete(
            @Valid @RequestBody ManagerRegisterRequest request
    ) {
        service.register(request);
        return ResponseResult.created();
    }


}
