package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.dto.DeviceDto;
import org.ouanu.manager.iface.PermissionCheck;
import org.ouanu.manager.query.DeviceQuery;
import org.ouanu.manager.request.RegisterDeviceRequest;
import org.ouanu.manager.response.DeviceResponse;
import org.ouanu.manager.response.DeviceTokenResponse;
import org.ouanu.manager.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/login")
    private ResponseEntity<ResponseResult<DeviceTokenResponse>> login(String uuid, String signature) {
        return loginDevice(uuid, signature);
    }

    private @NotNull ResponseEntity<ResponseResult<DeviceTokenResponse>> loginDevice(String uuid, String signature) {
        DeviceTokenResponse response = deviceService.authenticate(uuid, signature);
        if (Objects.requireNonNull(response.token()).isEmpty()) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Device not be found.");
        }
        return ResponseResult.success(
                response
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult<DeviceTokenResponse>> register(
            @Valid @RequestBody RegisterDeviceRequest request
    ) {
        if (deviceService.register(request)) {
            return loginDevice(request.uuid(), request.signature());
        } else {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Created device failed.");
        }

    }

    @PermissionCheck(roles = {"CUSTOMER"})
    @GetMapping("/list")
    public ResponseEntity<ResponseResult<List<DeviceResponse>>> listDevices(@RequestParam Map<String, String> params) {
        DeviceQuery.DeviceQueryBuilder query = createParams(params);
        return ResponseResult.success(deviceService.findByConditions(query.build()));
    }

    @PermissionCheck(roles = {"ADMIN"})
    @GetMapping("/list_admin")
    public ResponseEntity<ResponseResult<List<DeviceDto>>> listDevicesByAdmin(@RequestParam Map<String, String> params) {
        DeviceQuery.DeviceQueryBuilder query = createParams(params);
        return ResponseResult.success(deviceService.findByAdminConditions(query.build()));
    }

    private static DeviceQuery.DeviceQueryBuilder createParams(Map<String, String> params) {
        DeviceQuery.DeviceQueryBuilder query = DeviceQuery.builder();
        if (params.containsKey("uuid"))
            query.uuid(params.get("uuid"));
        if (params.containsKey("userUuid"))
            query.userUuid(params.get("userUuid"));
        if (params.containsKey("deviceName"))
            query.deviceName(params.get("deviceName"));
        if (params.containsKey("deviceGroup"))
            query.deviceGroup(params.get("deviceGroup"));
        if (params.containsKey("active"))
            query.active(params.get("active").equals("true"));
        if (params.containsKey("locked"))
            query.locked(params.get("locked").equals("true"));
        if (params.containsKey("startCreateTime") && params.containsKey("endCreateTime"))
            query.createTimeRange(new DeviceQuery.TimeRange(LocalDateTime.parse(params.get("startTime")), LocalDateTime.parse(params.get("endTime"))));
        if (params.containsKey("startModifiedTime") && params.containsKey("endModifiedTime"))
            query.lastModifiedTimeRange(new DeviceQuery.TimeRange(LocalDateTime.parse(params.get("startModifiedTime")), LocalDateTime.parse(params.get("endModifiedTime"))));
        return query;
    }

    @PermissionCheck(roles = {"ADMIN"})
    @PostMapping("/update_admin")
    public ResponseEntity<ResponseResult<DeviceDto>> updateDeviceByAdmin(@Valid @RequestBody DeviceDto dto) {
        DeviceDto result = deviceService.updateDeviceAdmin(dto);
        if (result == null) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Can't find the device.");
        }
        return ResponseResult.success(result);
    }

    @PermissionCheck(roles = {"ADMIN"})
    @PostMapping("/delete_admin")
    public ResponseEntity<ResponseResult<String>> deleteDeviceByAdmin(@Valid @RequestBody DeviceDto dto) {
        Boolean result = deviceService.deleteDeviceByAdmin(dto);
        if (!result) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Device can't be deleted.");
        }
        return ResponseResult.success("Delete Device: " + dto.getUuid() + " " + dto.getDeviceName() + " succeed.");
    }


    @GetMapping("/find")
    public ResponseEntity<ResponseResult<DeviceResponse>> find(@RequestParam Map<String, String> params) {
        if (params.containsKey("uuid")) {
            DeviceResponse device = deviceService.findDevice(params.get("uuid"));
            if (device != null) {
                return ResponseResult.success(device);
            }
        }
        return ResponseResult.error(HttpStatus.NOT_FOUND, "设备未找到");
    }

    @GetMapping("/item")
    public ResponseEntity<ResponseResult<DeviceResponse>> item() {
        DeviceResponse item = deviceService.getDeviceInfo();
        if (item == null) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Device can not be found");
        } else {
            return ResponseResult.success(item);
        }
    }

}
