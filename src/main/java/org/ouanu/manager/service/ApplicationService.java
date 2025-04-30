package org.ouanu.manager.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ApplicationDTO;
import org.ouanu.manager.dto.ApplicationUploadRequest;
import org.ouanu.manager.model.Application;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.repository.ApplicationRepository;
import org.ouanu.manager.repository.DeviceRepository;
import org.ouanu.manager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void uploadApplications(ApplicationUploadRequest request) {
        Device device = deviceRepository.findByUuid(request.getDeviceUuid())
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        Set<String> existingPackages = applicationRepository.findByDeviceUuid(request.getDeviceUuid())
                .stream()
                .map(Application::getPackageName)
                .collect(Collectors.toSet());
        List<Application> applicationsToSave = new ArrayList<>();
        for (ApplicationDTO appInfo : request.getApplications()) {
            Application application;
            if (existingPackages.contains(appInfo.getPackageName())) {
                application = applicationRepository.findByDeviceUuidAndPackageName(
                        request.getDeviceUuid(), appInfo.getPackageName()
                ).orElse(new Application());
            } else {
                application = new Application();
                application.setDevice(device);
                application.setPackageName(appInfo.getPackageName());
                application.setUpdateTime(LocalDateTime.now());
            }
            application.setAppName(appInfo.getAppName());
            application.setAppVersion(appInfo.getAppVersion());
            application.setIsSystemApp(appInfo.getIsSystemApp());
            if (!appInfo.getUpdateTime().isEmpty())
                application.setUpdateTime(LocalDateTime.parse(appInfo.getUpdateTime())); // 修改该属性可以促使应用强制更新
            applicationsToSave.add(application);
        }
        applicationRepository.saveAll(applicationsToSave);
    }

    public List<ApplicationDTO> getApplicationsByDeviceUuid(String deviceUuid) {
        List<ApplicationDTO> applicationDTOS = new ArrayList<>();
        for (Application application : applicationRepository.findByDeviceUuid(deviceUuid)) {
            ApplicationDTO appDto = new ApplicationDTO();
            appDto.setAppName(application.getAppName());
            appDto.setPackageName(application.getPackageName());
            appDto.setAppVersion(application.getAppVersion());
            appDto.setIsForceInstallation(application.getIsForceInstallation());
            appDto.setIsSystemApp(application.getIsSystemApp());
            applicationDTOS.add(appDto);
        }
        return applicationDTOS;
    }
}