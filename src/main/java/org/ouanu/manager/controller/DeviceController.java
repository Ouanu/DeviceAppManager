package org.ouanu.manager.controller;

import org.ouanu.manager.dto.DeviceDTO;
import org.ouanu.manager.dto.DeviceRegistrationRequest;
import org.ouanu.manager.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<DeviceDTO> registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        return ResponseEntity.ok(deviceService.registerDevice(request));
    }
}