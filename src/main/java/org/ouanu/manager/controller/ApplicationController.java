package org.ouanu.manager.controller;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.response.ApplicationResponse;
import org.ouanu.manager.service.ApplicationService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file, @RequestParam("ban_regions") String[] banRegions) {
        if (banRegions.length == 0) {
            banRegions = new String[]{""};
        }
        return service.uploadFile(file, banRegions);
    }

    @PostMapping("/multi-upload")
    public ResponseEntity<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("ban_regions") String[] banRegions) {
        if (banRegions.length == 0) {
            banRegions = new String[]{""};
        }
        return service.uploadFiles(files, banRegions);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return service.downloadFile(fileName);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ApplicationResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
