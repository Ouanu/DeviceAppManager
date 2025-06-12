package org.ouanu.manager.controller;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.service.ApplicationService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {
        return service.uploadFile(file);
    }

    @PostMapping("/multi-upload")
    public ResponseEntity<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return service.uploadFiles(files);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return service.downloadFile(fileName);
    }
}
