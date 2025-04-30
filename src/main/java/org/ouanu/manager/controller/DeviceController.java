package org.ouanu.manager.controller;

import org.ouanu.manager.dto.DeviceDTO;
import org.ouanu.manager.dto.DeviceRegistrationRequest;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<DeviceDTO> registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        return ResponseEntity.ok(deviceService.registerDevice(request));
    }

    @GetMapping("/user_devices")
    public ResponseEntity<List<DeviceDTO>> getUserDevices() {
        List<DeviceDTO> userDevices = deviceService.getUserDevices();
        System.out.println("search user's deivces completely.");
        return ResponseEntity.ok(userDevices);
    }

    /**
     * 分页获取用户的设备
     */
    @GetMapping("/page")
    public ResponseEntity<List<DeviceDTO>> getUserDevicesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "registrationTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        System.out.println("getUserDevicesPaged -------------------");
        return ResponseEntity.ok(deviceService.getUserDevicesPageable(page, size, sortBy, direction));
    }

    /**
     * 分页搜索用户设备
     */
    @GetMapping("/search")
    public ResponseEntity<List<DeviceDTO>> searchUserDevices(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(deviceService.getUserDevicesWithKeyword(keyword, page, size));
    }
}