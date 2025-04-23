package org.ouanu.manager.service;

import org.ouanu.manager.dto.DeviceDTO;
import org.ouanu.manager.dto.DeviceRegistrationRequest;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.DeviceRepository;
import org.ouanu.manager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    // Register a new device or update the infomation of the device.
    @Transactional
    public DeviceDTO registerDevice(DeviceRegistrationRequest request) {
        User user = userRepository.findByUuid(request.getUserUuid())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Optional<Device> existsingDevice = deviceRepository.findByUuid(request.getDeviceUuid());

        Device device;
        if (existsingDevice.isPresent()) {
            // Update the infomation of the device.
            device = existsingDevice.get();
            device.setDeviceName(request.getDeviceName());
            device.setModel(request.getModel());
            device.setBrand(request.getBrand());
            device.setImei(request.getImei());
            device.setAndroidVersion(request.getAndroidVersion());
        } else {
            // Create a new device.
            device = new Device();
            device.setUuid(request.getDeviceUuid());
            device.setUser(user);
            device.setDeviceName(request.getDeviceName());
            device.setModel(request.getModel());
            device.setBrand(request.getBrand());
            device.setImei(request.getImei());
            device.setAndroidVersion(request.getAndroidVersion());
            device.setRegistrationTime(LocalDateTime.now());
        }
        device.setLastActiveTime(LocalDateTime.now());
        return mapToDTO(deviceRepository.save(device));
    }



    private DeviceDTO mapToDTO(Device device) {
        return new DeviceDTO(
                device.getUuid(),
                device.getDeviceName(),
                device.getModel(),
                device.getBrand(),
                device.getAndroidVersion(),
                device.getRegistrationTime(),
                device.getLastActiveTime()
                );
    }
}