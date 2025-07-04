package org.ouanu.manager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.apk.ApkManifestReader;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.exception.ApplicationNotFoundException;
import org.ouanu.manager.model.Application;
import org.ouanu.manager.query.ApplicationQuery;
import org.ouanu.manager.repository.AppRepository;
import org.ouanu.manager.request.ApplicationInfoRequest;
import org.ouanu.manager.response.ApplicationResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ApplicationService {

    private final AppRepository repository;
    private final EntityManager entityManager;
    private final TempDataService service;

    private static final String uploadDir = "./uploads/";
    private static final String tempDir = "./tmp/";
    private static final String iconsDir = "./icons/";
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

        File dir1 = new File(uploadDir);
        File dir2 = new File(tempDir);
        File dir3 = new File(iconsDir);
        if (!dir1.exists() || !dir1.isDirectory()) dir1.mkdirs();
        if (!dir2.exists() || !dir2.isDirectory()) dir2.mkdirs();
        if (!dir3.exists() || !dir3.isDirectory()) dir3.mkdirs();
    }

    public ApplicationResponse findApplicationByPackageName(String packageName) {
        Application application = repository.findByPackageName(packageName).orElseThrow(() -> new ApplicationNotFoundException(HttpStatus.NOT_FOUND, "App is not exists."));
        return ApplicationResponse.fromEntity(application);
    }

    public ResponseEntity<ResponseResult<List<ApplicationResponse>>> findAll() {
        List<ApplicationResponse> list = repository.findAll().stream().map(ApplicationResponse::fromEntity).toList();
        if (!list.isEmpty()) {
            return ResponseResult.success(list);
        } else {
            return ResponseResult.error(HttpStatus.NOT_FOUND, null);
        }
    }

    public ResponseEntity<ResponseResult<String>> deleteByPackageName(String packageName) {
        if (repository.deleteByPackageName(packageName) > 0) {
            return ResponseResult.success("Delete " + packageName + " succeed.");
        } else {
            return ResponseResult.error(HttpStatus.EXPECTATION_FAILED, "Can not delete " + packageName + " . Maybe it does not exists.");
        }
    }

    public ResponseEntity<ResponseResult<ApplicationResponse>> update(ApplicationInfoRequest request) {
        String packageName = request.packageName();
        Optional<Application> applicationOptional = repository.findByPackageName(packageName);
        if (applicationOptional.isEmpty()) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "Cannot find the target app. Please check the param.");
        } else {
            Application app = applicationOptional.get();
            app.setLabel(request.label());
            app.setVersionName(request.versionName());
            app.setVersionCode(request.versionCode());
            app.setUploadTime(LocalDateTime.now());
            app.setSize(request.size());
            app.setAppNames(request.appNames());
            if (!app.getFileName().equals(request.fileName()) && request.fileName().isBlank()) {
                File newApk = new File(uploadDir, request.fileName());
                if (!newApk.exists() || !newApk.isFile()) {
                    return ResponseResult.error(HttpStatus.BAD_REQUEST, "APK does not exists.");
                }
                File apk = new File(uploadDir, app.getFileName());
                boolean delete = apk.delete();
                logger.info("The result of deleting the apk: " + packageName + " is " + delete);
            } else {
                return ResponseResult.error(HttpStatus.BAD_REQUEST, "File name does not exists.");
            }
            app.setFileName(request.fileName());
            return ResponseResult.success("The operation was successful, " +
                            "but this modification operation is not recommended.",
                    ApplicationResponse.fromEntity(repository.save(app)));
        }
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

    public ResponseEntity<ResponseResult<ApplicationResponse>> uploadApkRequireConfirm(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.info("The file does not exist.Please select a file to upload.");
            return ResponseResult.error(HttpStatus.BAD_REQUEST, "The file does not exist.Please select a file to upload.", null);
        }
        String source = tempDir + file.getOriginalFilename();
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".apk")) {
            logger.info("The file does not end with '.apk'. Please check the file format.");
            return ResponseResult.error(HttpStatus.BAD_REQUEST, "The file does not end with '.apk'. Please check the file format.", null);
        }
        Path path = Path.of(source);
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            String packageName = ApkManifestReader.readPackageName(new File(source));
            if (packageName.isEmpty()) {
                Files.delete(path);
                logger.info("File: " + source + " is not an apk.");
                return ResponseResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "File: " + source + " is not an apk.", null);
            }
            String target = tempDir + packageName + ".apk";
            String result = renameFile(target, source);
            Application application = ApkManifestReader.readApplicationInfo(new File(result));
            if (application != null) {
                service.store(application.getFileName(), application);
                return ResponseResult.success("Upload a file succeed: " + result, ApplicationResponse.fromEntity(application));
            } else {
                logger.info("Upload File: Cannot create or save the application information.");
                return ResponseResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Upload File: Cannot create or save the application information.", null);
            }
        } catch (IOException e) {
            logger.info("Upload a file failed.");
            return ResponseResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Upload a file failed.", null);
        }
    }

    public ResponseEntity<ResponseResult<String>> confirmAndSave(ApplicationInfoRequest request) {
        File source = new File(tempDir, request.fileName());
        File target = new File(uploadDir, request.fileName());
        if (!source.exists() || !source.isFile()) {
            return ResponseResult.error(HttpStatus.NOT_FOUND, "File: " + source.getName() + " does not exists.");
        }
        Application appCache = service.get(request.fileName(), Application.class);
        if (appCache != null) {
            String s = renameFile(target.getAbsolutePath(), source.getAbsolutePath());
            if (s.isEmpty()) {
                return ResponseResult.error(HttpStatus.NOT_FOUND, "File: " + s + " is not valid.");
            }
            File retFile = new File(s);
            if (!retFile.exists() || !retFile.isFile()) {
                return ResponseResult.error(HttpStatus.NOT_FOUND, "FileName: " + s + " does not exists.");
            }
            if (!request.label().isEmpty()) {
                appCache.setLabel(request.label());
            }
            if (!request.appNames().isEmpty()) {
                appCache.setAppNames(request.appNames());
            }
            if (!request.fileName().isEmpty()) {
                appCache.setFileName(request.fileName());
            }
            Optional<Application> applicationOptional = repository.findByPackageName(appCache.getPackageName());
            if (applicationOptional.isPresent()) {
                Application application = applicationOptional.get();
                application.setLabel(appCache.getLabel());
                application.setSize(appCache.getSize());
                application.setAppNames(appCache.getAppNames());
                application.setVersionCode(appCache.getVersionCode());
                application.setVersionName(appCache.getVersionName());
                application.setBanRegions(appCache.getBanRegions());
                application.setUploadTime(LocalDateTime.now());
                try {
                    Files.deleteIfExists(Path.of(source.toURI()));
                } catch (IOException e) {
                    logger.info("delete " + source.getName() + " failed. " + e.getMessage());
                }
                repository.save(application);
            } else {
                repository.save(appCache);
            }
            service.remove(appCache.getPackageName());
            if (repository.existsByPackageName(appCache.getPackageName())) {
                return ResponseResult.success("File: " + appCache.getPackageName() + " has been saved.");
            }
            return ResponseResult.error(HttpStatus.NOT_FOUND, "File: " + appCache.getPackageName() + " has not been saved.");
        }
        return ResponseResult.error(HttpStatus.NOT_FOUND, "APK Info: " + request.packageName() + " cannot be found.");
    }


    public ResponseEntity<ResponseResult<List<ApplicationResponse>>> uploadMultipleApksRequireConfirm(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseResult.error(HttpStatus.BAD_REQUEST, "Please select at least one file for upload.", null);
        }
        StringBuilder message = new StringBuilder();
        boolean allSuccess = true;
        List<ApplicationResponse> appList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (!Objects.requireNonNull(fileName).endsWith(".apk")) {
                message.append("File: ").append(file.getOriginalFilename()).append(" does not end with '.apk'. Please check the file format.\n");
                continue;
            }
            String source = tempDir + fileName;
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
                String target = tempDir + packageName + ".apk";
                String result = renameFile(target, source);
                Application application = ApkManifestReader.readApplicationInfo(new File(result));
                if (application != null) {
                    service.store(application.getFileName(), application);
                    appList.add(ApplicationResponse.fromEntity(application));
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
            return ResponseResult.success(message.toString(), appList);
        } else {
            return ResponseResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Some files failed to upload: \n" + message, null);
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

    public ResponseEntity<Resource> downloadIcon(@PathVariable String iconName) {
        try {
            System.out.println("iconName = " + iconName);
            Path filePath = Paths.get(iconsDir).resolve(iconName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; file=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("Download the file error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private final ConcurrentMap<Path, Boolean> activeOperations = new ConcurrentHashMap<>();
    private final Object fileOperationLock = new Object();

    private String renameFile(String targetPath, String sourcePath) {
        Path target = Paths.get(targetPath);
        Path source = Paths.get(sourcePath);
        activeOperations.put(source, true);

        try {
            synchronized (fileOperationLock) {
                Path parentDir = target.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);

                return target.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Renaming failed: " + sourcePath + " -> " + targetPath, e);
        } finally {
            activeOperations.remove(source);
        }
    }


    @Scheduled(fixedRate = 15 * 60000)
    public void cleanTempFiles() {
        Path tempPath = Paths.get(tempDir);
        try (Stream<Path> pathStream = Files.list(tempPath)) {
            List<Path> filesToProcess = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            if (activeOperations.containsKey(path)) {
                                return false;
                            }
                            Instant lastModified = Files.getLastModifiedTime(path).toInstant();
                            return lastModified.isBefore(Instant.now().minus(15, ChronoUnit.MINUTES));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
            filesToProcess.stream()
                    .filter(path -> {
                        try {
                            return !isFileLocked(path) && !activeOperations.containsKey(path);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            logger.info("clear the cache file: " + path);
                        } catch (IOException e) {
                            logger.warning("clear the cache file failed: " + path + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.warning("Directory traversal failed: " + e.getMessage());
        }
        logger.info("The temporary files have been cleared");
    }

    private boolean isFileLocked(Path file) {
        try (FileInputStream fis = new FileInputStream(file.toFile());
             FileChannel channel = fis.getChannel()) {
            FileLock lock = channel.tryLock(0L, Long.MAX_VALUE, true);
            if (lock != null) {
                lock.release();
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}
