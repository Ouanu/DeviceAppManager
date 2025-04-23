package org.ouanu.manager.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ApplicationDTO;
import org.ouanu.manager.dto.ApplicationUploadRequest;
import org.ouanu.manager.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping("/{deviceUuid}/applications")
    public ResponseEntity<Void> uploadApplications(
            @PathVariable String deviceUuid,
            @Valid @RequestBody List<ApplicationDTO> applicationInfos
    ) {
        ApplicationUploadRequest request = new ApplicationUploadRequest(deviceUuid, applicationInfos);
        applicationService.uploadApplications(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deviceUuid}/applications")
    public ResponseEntity<List<ApplicationDTO>> getDeviceApplications(@PathVariable String deviceUuid) {
        return ResponseEntity.ok(applicationService.getApplicationsByDeviceUuid(deviceUuid));
    }
}