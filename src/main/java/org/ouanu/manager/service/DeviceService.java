package org.ouanu.manager.service;

import org.ouanu.manager.dto.DeviceDTO;
import org.ouanu.manager.dto.DeviceRegistrationRequest;
import org.ouanu.manager.exception.AppException;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.DeviceRepository;
import org.ouanu.manager.repository.UserRepository;
import org.ouanu.manager.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, JwtService jwtService) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

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
            device.setDeviceGroup(request.getDeviceGroup());
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
            device.setDeviceGroup(request.getDeviceGroup());
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
                device.getDeviceGroup(),
                device.getModel(),
                device.getBrand(),
                device.getAndroidVersion(),
                device.getRegistrationTime(),
                device.getLastActiveTime()
        );
    }

    public List<DeviceDTO> getUserDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));

        List<DeviceDTO> devices = new ArrayList<>();
        for (Device device : user.getDevices()) {
            DeviceDTO dto = new DeviceDTO();
            dto.setUuid(device.getUuid());
            dto.setDeviceName(device.getDeviceName());
            dto.setDeviceGroup(device.getDeviceGroup());
            dto.setModel(device.getModel());
            dto.setBrand(device.getBrand());
            dto.setAndroidVersion(device.getAndroidVersion());
            dto.setRegistrationTime(device.getRegistrationTime());
            dto.setLastActiveTime(device.getLastActiveTime());
            devices.add(dto);
        }
        return devices;
    }

    public boolean removeDeviceByDeviceUuid(String uuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new AppException("无法删除，该设备不存在", HttpStatus.NOT_FOUND));
        if (device != null) {
            if (!Objects.equals(device.getUser().getUuid(), user.getUuid()))
                 return false;
            int size = deviceRepository.deleteByUuid(uuid);
            return size > 0;
        } else {
            return false;
        }
    }

    /**
     * 分页查询用户的设备
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param direction 排序方向
     * @return 设备分页结果
     */
    public List<DeviceDTO> getUserDevicesPageable(int page, int size, String sortBy, String direction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        System.out.println("getUserDevicesPageable -----------------");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Device> devicePage = deviceRepository.findByUserUuidPageable(user.getUuid(), pageable);
        List<DeviceDTO> devices = new ArrayList<>();
        for (Device device : devicePage) {
            DeviceDTO dto = new DeviceDTO();
            dto.setUuid(device.getUuid());
            dto.setDeviceName(device.getDeviceName());
            dto.setDeviceGroup(device.getDeviceGroup());
            dto.setModel(device.getModel());
            dto.setBrand(device.getBrand());
            dto.setAndroidVersion(device.getAndroidVersion());
            dto.setRegistrationTime(device.getRegistrationTime());
            dto.setLastActiveTime(device.getLastActiveTime());
            devices.add(dto);
        }
        return devices;
    }

    /**
     * 带关键词的分页查询
     * @param keyword 关键词
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 设备分页结果
     */
    public List<DeviceDTO> getUserDevicesWithKeyword(String keyword, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("用户不存在", HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Device> devicePage = deviceRepository.findByUserUuidAndKeyword(user.getUuid(), keyword, pageable);
        List<DeviceDTO> devices = new ArrayList<>();
        for (Device device : devicePage) {
            DeviceDTO dto = new DeviceDTO();
            dto.setUuid(device.getUuid());
            dto.setDeviceName(device.getDeviceName());
            dto.setDeviceGroup(device.getDeviceGroup());
            dto.setModel(device.getModel());
            dto.setBrand(device.getBrand());
            dto.setAndroidVersion(device.getAndroidVersion());
            dto.setRegistrationTime(device.getRegistrationTime());
            dto.setLastActiveTime(device.getLastActiveTime());
            devices.add(dto);
        }
        return devices;
    }
}