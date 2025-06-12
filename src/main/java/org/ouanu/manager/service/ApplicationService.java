package org.ouanu.manager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.apk.ApkManifestReader;
import org.ouanu.manager.exception.ApplicationNotFoundException;
import org.ouanu.manager.model.Application;
import org.ouanu.manager.query.ApplicationQuery;
import org.ouanu.manager.repository.AppRepository;
import org.ouanu.manager.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final AppRepository repository;
    private final EntityManager entityManager;

    private final String uploadDir = "./tmp/uploads/";

    public ApplicationResponse findApplicationByPackageName(String packageName) {
        Application application = repository.findByPackageName(packageName).orElseThrow(() -> new ApplicationNotFoundException(HttpStatus.NOT_FOUND, "App is not exists."));
        return ApplicationResponse.fromEntity(application);
    }

    public List<ApplicationResponse> findAll() {
        return repository.findAll().stream().map(ApplicationResponse::fromEntity).toList();
    }

    public List<ApplicationResponse> findByConditions(ApplicationQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Application> cq = cb.createQuery(Application.class);
        Root<Application> root = cq.from(Application.class);
        List<Predicate> predicates = new ArrayList<>();
        if (query != null && StringUtils.hasText(query.getPackageName())) {
            predicates.add(cb.like(root.get("packageName"), query.getPackageName()));
        }
        if (query != null && StringUtils.hasText(query.getAppName())) {
            predicates.add(cb.like(root.get("appName"), query.getAppName()));
        }
        if (query != null && StringUtils.hasText(query.getLabel())) {
            predicates.add(cb.like(root.get("label"), query.getLabel()));
        }
        if (query != null && query.getUploadTimeRange() != null) {
            if (query.getUploadTimeRange().getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("uploadTime"), query.getUploadTimeRange().getStart()
                ));
            }
            if (query.getUploadTimeRange().getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("uploadTime"), query.getUploadTimeRange().getEnd()
                ));
            }
        }
        String banRegion;
        if (query != null)
            banRegion = query.getBanRegion();
        else
            banRegion = "";
        cq.where(predicates.toArray(new Predicate[0]));
        List<Application> resultList = entityManager.createQuery(cq).getResultList();
        List<ApplicationResponse> list = new ArrayList<>();
        for (Application application : resultList) {
            if (Arrays.stream(application.getBanRegions()).toList().contains(banRegion)) {
                continue;
            }
            ApplicationResponse applicationResponse = ApplicationResponse.fromEntity(application);
            list.add(applicationResponse);
        }
        return list;
    }

    public ResponseEntity<String> uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file upload.");
        }
        if (!file.getName().endsWith(".apk")) {
            return ResponseEntity.badRequest().body("The file is not an APK.");
        }
        try {
            String fileName = file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, bytes);
            String packageName = ApkManifestReader.readPackageName(new File(uploadDir + fileName));
            if (packageName.isEmpty()) {
                Files.delete(path);
                return ResponseEntity.internalServerError().body("Upload a file failed.");
            }
            String target = renameFile(uploadDir + packageName + ".apk", uploadDir + fileName);
            return ResponseEntity.ok("Upload a file succeed: " + target);
        } catch (IOException e) {
            System.out.println("Upload a file failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Upload a file failed.");
        }
    }

    public ResponseEntity<String> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("Please select at least one file for upload.");
        }

        StringBuilder message = new StringBuilder();
        boolean allSuccess = true;
        for (MultipartFile file : files) {
            try {
                if (file.getName().endsWith(".apk")) {
                    String fileName = file.getOriginalFilename();
                    byte[] bytes = file.getBytes();
                    Path path = Paths.get(uploadDir + fileName);
                    Files.write(path, bytes);
                    String packageName = ApkManifestReader.readPackageName(new File(uploadDir + fileName));
                    if (packageName.isEmpty()) {
                        message.append("File ").append(file.getName()).append(" uploaded failed\n");
                        Files.delete(path);
                        continue;
                    }
                    String target = renameFile(uploadDir + packageName + ".apk", uploadDir + fileName);
                    message.append("File ").append(target).append(" uploaded succeed\n");
                    continue;
                }
            } catch (IOException e) {
                System.out.println("Upload the file failed: " + e.getMessage());
                allSuccess = false;
            }
            message.append("File ").append(file.getName()).append(" uploaded failed\n");
        }
        if (allSuccess) {
            return ResponseEntity.ok(message.toString());
        } else {
            return ResponseEntity.internalServerError()
                    .body("部分文件上传失败: \n" + message);
        }
    }

    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; file=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("Download the file error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String renameFile(String newName, String originalName) {
        try {
            Path source = Paths.get(uploadDir, originalName);
            Path target = Paths.get(uploadDir, newName);

            if (!Files.exists(target.getParent())) {
                Files.createDirectories(target.getParent());
            }

            Files.move(source, target);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("File renames failed: " + e.getMessage());
        }
    }

}
