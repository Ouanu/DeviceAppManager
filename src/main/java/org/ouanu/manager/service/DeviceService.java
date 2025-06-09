package org.ouanu.manager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.command.DeviceCreateCommand;
import org.ouanu.manager.common.DeviceAuthentication;
import org.ouanu.manager.dto.DeviceDto;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.exception.DeviceNotFoundException;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.query.DeviceQuery;
import org.ouanu.manager.repository.DeviceRepository;
import org.ouanu.manager.request.RegisterDeviceRequest;
import org.ouanu.manager.response.DeviceResponse;
import org.ouanu.manager.response.DeviceTokenResponse;
import org.ouanu.manager.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final JwtUtils jwtUtils;
    private final EntityManager entityManager;

    public DeviceTokenResponse authenticate(String uuid, String signature) {
        if (!deviceRepository.existsByUuid(uuid)) {
            return new DeviceTokenResponse("");
        }
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device not found."));
        if (device != null) {
            DeviceAuthentication auth = new DeviceAuthentication(uuid, signature, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
            loginUpdateLastModifiedTime(uuid);
            return new DeviceTokenResponse(jwtUtils.deviceGenerateJwtToken(auth));
        } else {
            throw new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device not found.");
        }
    }

    @Transactional
    public boolean createDevice(DeviceCreateCommand command) {
        try {
            if (deviceRepository.existsByUuid(command.uuid())) {
                throw new ConflictException("Device is already exists.");
            }

            if (!command.signature().equals("Fq9gzy85kxbpdNPIEHjNOv7TwkhZIsieT3qmQ1Dg10k=")) {
                throw new AccessDeniedException("Access Denied");
            }

            Device device = command.toEntity();
            device.setCreateTime(LocalDateTime.now());
            device.setLastModifiedTime(LocalDateTime.now());
            device.setSignature(command.signature());
            deviceRepository.save(device);

            if (!deviceRepository.existsByUuid(device.getUuid())) {
                System.out.println("Device Service: createDevice(DeviceCreateCommand command) >> Create device failed.");
                throw new ConflictException("Failed to create the device.");
            }

            System.out.println("Device Service: createDevice(DeviceCreateCommand command) >> Create device succeed.");
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    @Transactional
    public void loginUpdateLastModifiedTime(String uuid) {
        deviceRepository.updateLoginTime(uuid, LocalDateTime.now());
    }

    public DeviceDto loadDeviceDtoByUuid(String uuid) throws DeviceNotFoundException{
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "设备未找到"));
        DeviceDto dto = new DeviceDto();
        dto.setUuid(device.getUuid());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceGroup(device.getDeviceGroup());
        dto.setRemark(device.getRemark());
        dto.setActive(device.isActive());
        dto.setLocked(device.isLocked());
        dto.setCreateTime(device.getCreateTime());
        dto.setLastModifiedTime(device.getLastModifiedTime());
        return dto;
    }

    public Device loadDeviceByUuid(String uuid) throws DeviceNotFoundException {
        return deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "设备未找到"));
    }

    public DeviceResponse findDevice(String uuid) throws DeviceNotFoundException {
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "设备未找到"));
        return DeviceResponse.fromEntity(device);
    }

    public List<DeviceResponse> findByConditions(DeviceQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Device> cq = cb.createQuery(Device.class);
        Root<Device> root = cq.from(Device.class);
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(query.getUuid())) {
            predicates.add(cb.like(root.get("uuid"), query.getUuid()));
        }
        if (StringUtils.hasText(query.getUserUuid())) {
            predicates.add(cb.like(root.get("userUuid"), query.getUserUuid()));
        }
        if (StringUtils.hasText(query.getDeviceName())) {
            predicates.add(cb.like(root.get("deviceName"), "%" + query.getDeviceName() + "%"));
        }
        if (StringUtils.hasText(query.getDeviceGroup())) {
            predicates.add(cb.like(root.get("deviceGroup"), "%" + query.getDeviceGroup() + "%"));
        }
        if (query.getActive() != null) {
            predicates.add(cb.equal(root.get("active"), query.getActive()));
        }
        if (query.getLocked() != null) {
            predicates.add(cb.equal(root.get("locked"), query.getLocked()));
        }
        if (query.getCreateTimeRange() != null) {
            if (query.getCreateTimeRange().getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createTime"), query.getCreateTimeRange().getStart()));
            }
            if (query.getCreateTimeRange().getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createTime"), query.getCreateTimeRange().getEnd()));
            }
        }
        if (query.getLastModifiedTimeRange() != null) {
            if (query.getLastModifiedTimeRange().getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("lastModifiedTime"), query.getLastModifiedTimeRange().getStart()));
            }
            if (query.getLastModifiedTimeRange().getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("lastModifiedTime"), query.getLastModifiedTimeRange().getEnd()));
            }
        }
        cq.where(predicates.toArray(new Predicate[0]));
        List<Device> devices = entityManager.createQuery(cq).getResultList();
        List<DeviceResponse> list = new ArrayList<>();
        for (Device device : devices) {
            DeviceResponse dto = DeviceResponse.fromEntity(device);
            list.add(dto);
        }
        return list;
    }

    public boolean register(@Valid RegisterDeviceRequest request) {
        DeviceCreateCommand command = request.toCommand();
        return createDevice(command);
    }

    public DeviceResponse item() {
        try {
            System.out.println("DeviceService ================ ");
            Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
            String principal = (String) auth.getPrincipal();
            String credentials = (String) auth.getCredentials();
            System.out.println("principal = " + principal + " credentials = " + credentials);
            Device device = deviceRepository.findByUuid(principal).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device can not be found"));
            if (device.getSignature().equals(credentials)) {
                return DeviceResponse.fromEntity(device);
            }
            return null;
        } catch (Exception e) {
            System.out.println("DeviceService error = " + e.getMessage());
            return null;
        }


    }

//    private boolean checkPermission(String userUuid) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        UserDetails userDetails = (UserDetails) auth.getPrincipal();
//        Optional<User> byUsername = userRepository.findByUsername(userDetails.getUsername());
//        if (byUsername.isEmpty()) {
//            return false;
//        }
//        if (!userRepository.existsByUsername(userDetails.getUsername())) {
//            return false;
//        }
//        if (userRepository.existsByUuid(userUuid)) {
//            return false;
//        }
//    }
}
