package org.ouanu.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.request.ApplicationInfoRequest;
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
    public ResponseEntity<ResponseResult<ApplicationResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        return service.uploadApkRequireConfirm(file);
    }

    @PostMapping("/multi-upload")
    public ResponseEntity<ResponseResult<List<ApplicationResponse>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return service.uploadMultipleApksRequireConfirm(files);
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseResult<String>> saveOne(@Valid @RequestBody ApplicationInfoRequest appInfo) {
        return service.confirmAndSave(appInfo);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return service.downloadFile(fileName);
    }

    @GetMapping("/icon/{iconName:.+}")
    public ResponseEntity<Resource> downloadIcon(@PathVariable String iconName) {
        return service.downloadIcon(iconName);
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseResult<List<ApplicationResponse>>> findAll() {
        return service.findAll();
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseResult<String>> deleteByPackageName(@RequestParam("package_name") String packageName) {
        return service.deleteByPackageName(packageName);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseResult<ApplicationResponse>> update(@Valid @RequestBody ApplicationInfoRequest request) {
        return service.update(request);
    }


}
