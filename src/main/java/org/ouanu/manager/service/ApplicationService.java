package org.ouanu.manager.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.dto.ApplicationDTO;
import org.ouanu.manager.dto.ApplicationUploadRequest;
import org.ouanu.manager.model.Application;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.repository.ApplicaitonRepository;
import org.ouanu.manager.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicaitonRepository applicaitonRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void uploadApplications(ApplicationUploadRequest request) {
        Device device = deviceRepository.findByUuid(request.getDeviceUuid())
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        Set<String> existingPackages = applicaitonRepository.findByDeviceUuid(request.getDeviceUuid())
                .stream()
                .map(Application::getPackageName)
                .collect(Collectors.toSet());
        List<Application> applicationsToSave = new ArrayList<>();
        for (ApplicationDTO appInfo : request.getApplications()) {
            Application application;
            if (existingPackages.contains(appInfo.getPackageName())) {
                application = applicaitonRepository.findByDeviceUuidAndPackageName(
                        request.getDeviceUuid(), appInfo.getPackageName()
                ).orElse(new Application());
            } else {
                application = new Application();
                application.setDevice(device);
                application.setPackageName(appInfo.getPackageName());
            }
            application.setAppName(appInfo.getAppName());
            application.setAppVersion(appInfo.getAppVersion());
            application.setIsSystemApp(appInfo.getIsSystemApp());
            applicationsToSave.add(application);
        }
        applicaitonRepository.saveAll(applicationsToSave);
    }

    public List<ApplicationDTO> getApplicationsByDeviceUuid(String deviceUuid) {
        List<ApplicationDTO> applicationDTOS = new ArrayList<>();
        for (Application application : applicaitonRepository.findByDeviceUuid(deviceUuid)) {
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