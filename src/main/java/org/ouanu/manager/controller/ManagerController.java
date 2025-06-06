package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.iface.PermissionCheck;
import org.ouanu.manager.query.UserQuery;
import org.ouanu.manager.request.DeleteUserOrManagerRequest;
import org.ouanu.manager.request.RegisterManagerRequest;
import org.ouanu.manager.service.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService service;

    // 管理员获取所有的用户
    @PermissionCheck(roles = {"ADMIN"})
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

    // 注册新的用户
    @PostMapping("/register")
    public ResponseEntity<ResponseResult<Void>> register(
            @Valid @RequestBody RegisterManagerRequest request
    ) {
        service.register(request);
        return ResponseResult.created();
    }

    // 软删除用户
    @PermissionCheck(roles = {"ADMIN"})
    @PostMapping("/delete")
    public ResponseEntity<ResponseResult<Boolean>> delete(@Valid @RequestBody DeleteUserOrManagerRequest request) {
        boolean success = service.delete(request);
        return success ?
                ResponseResult.success() :
                ResponseResult.error(HttpStatus.NOT_FOUND, "用户不存在");
    }

    // 硬删除用户
    @PermissionCheck(roles = {"ADMIN"})
    @PostMapping("/hard_delete")
    public ResponseEntity<ResponseResult<Boolean>> hardDelete(@Valid @RequestBody DeleteUserOrManagerRequest request) {
        boolean success = service.hardDelete(request);
        return success ?
                ResponseResult.success() :
                ResponseResult.error(HttpStatus.NOT_FOUND, "用户不存在");
    }
}
