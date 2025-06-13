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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final AppRepository repository;
    private final EntityManager entityManager;

    private final String uploadDir = "./tmp/uploads/";
    private static final Logger logger = Logger.getLogger(ApplicationService.class.getName());
    static {
        Handler handler = new ConsoleHandler();
        try {
            handler.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

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
            if (application.getBanRegions().contains(banRegion)) {
                continue;
            }
            ApplicationResponse applicationResponse = ApplicationResponse.fromEntity(application);
            list.add(applicationResponse);
        }
        return list;
    }

    // Upload a single file.
    public ResponseEntity<String> uploadFile(MultipartFile file, String[] banRegions) {
        if (file == null || file.isEmpty()) {
            logger.info("The file does not exist.Please select a file to upload.");
            return ResponseEntity.badRequest().body("The file does not exist.Please select a file to upload.");
        }
        String source = uploadDir + file.getOriginalFilename();
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".apk")) {
            logger.info("The file does not end with '.apk'. Please check the file format.");
            return ResponseEntity.badRequest().body("The file does not end with '.apk'. Please check the file format.");
        }
        Path path = Path.of(source);
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            String packageName = ApkManifestReader.readPackageName(new File(source));
            if (packageName.isEmpty()) {
                Files.delete(path);
                logger.info("File: " + source + " is not an apk.");
                return ResponseEntity.internalServerError().body("File: " + source + " is not an apk.");
            }
            String target = uploadDir + packageName + ".apk";
            String result = renameFile(target, source);
            Application application = ApkManifestReader.readApplicationInfo(new File(result), banRegions);
            if (application != null) {
                Optional<Application> existingApp = repository.findByPackageName(application.getPackageName());
                if (existingApp.isPresent()) {
                    Application app = existingApp.get();
                    app.setSize(application.getSize());
                    app.setVersionName(application.getVersionName());
                    app.setVersionCode(application.getVersionCode());
                    app.setBanRegions(application.getBanRegions());
                    app.setLabel(application.getLabel());
                    app.setAppNames(application.getLabel());
                    app.setUploadTime(LocalDateTime.now());
                    repository.save(app);
                } else {
                    repository.save(application);
                }
                logger.info("Upload a file succeed: " + result);
                return ResponseEntity.ok("Upload a file succeed: " + result);
            } else {
                logger.info("Upload File: Cannot create or save the application information.");
                return ResponseEntity.internalServerError().body("Upload File: Cannot create or save the application information.");
            }
        } catch (IOException e) {
            logger.info("Upload a file failed.");
            return ResponseEntity.internalServerError().body("Upload a file failed.");
        }
    }

    // Upload multiple files.
    public ResponseEntity<String> uploadFiles(MultipartFile[] files, String[] banRegions) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("Please select at least one file for upload.");
        }
        StringBuilder message = new StringBuilder();
        boolean allSuccess = true;
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (!Objects.requireNonNull(fileName).endsWith(".apk")) {
                message.append("File: ").append(file.getOriginalFilename()).append(" does not end with '.apk'. Please check the file format.\n");
                continue;
            }
            String source = uploadDir + fileName;
            Path path = Path.of(source);
            try {
                byte[] bytes = file.getBytes();
                Files.write(path, bytes);
                String packageName = ApkManifestReader.readPackageName(new File(source));
                if (packageName.isEmpty()) {
                    Files.delete(path);
                    message.append("File: ").append(fileName).append(" is not an apk.\n");
                    continue;
                }
                String target = uploadDir + packageName + ".apk";
                String result = renameFile(target, source);
                Application application = ApkManifestReader.readApplicationInfo(new File(result), banRegions);
                if (application != null) {
                    Optional<Application> existingApp = repository.findByPackageName(application.getPackageName());
                    if (existingApp.isPresent()) {
                        Application app = existingApp.get();
                        app.setSize(application.getSize());
                        app.setVersionName(application.getVersionName());
                        app.setVersionCode(application.getVersionCode());
                        app.setBanRegions(application.getBanRegions());
                        app.setLabel(application.getLabel());
                        app.setAppNames(application.getLabel());
                        app.setUploadTime(LocalDateTime.now());
                        repository.save(app);
                    } else {
                        repository.save(application);
                    }
                    message.append("Upload a file succeed: ").append(result).append(".\n");
                } else {
                    Files.delete(Path.of(result));
                    message.append("File: ").append(fileName).append(" Cannot create or save the application information.\n");
                }
            } catch (IOException e) {
                message.append("File: ").append(file.getName()).append(" uploaded failed\n");
                try {
                    Files.delete(path);
                } catch (IOException ex) {
                    message.append("cannot delete file: ").append(path).append("\n").append(ex).append("\n");
                }
                allSuccess = false;
            }
        }
        logger.info(message.toString());
        if (allSuccess) {
            return ResponseEntity.ok(message.toString());
        } else {
            return ResponseEntity.internalServerError()
                    .body("Some files failed to upload: \n" + message);
        }
    }

    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            System.out.println("fileName = " + fileName);
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

    private String renameFile(String targetPath, String sourcePath) {
        try {
            Path target = Paths.get(targetPath);
            Files.deleteIfExists(target);
            Path source = Paths.get(sourcePath);
            System.out.println("source = " + source + " target = " + target);
            if (!Files.exists(target.getParent())) {
                Files.createDirectories(target.getParent());
                throw new RuntimeException("Cannot rename : " + sourcePath + " to " + targetPath);
            }
            Files.move(source, target);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("File renames failed: " + e.getMessage());
        }
    }

}
