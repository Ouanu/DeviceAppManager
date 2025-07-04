package org.ouanu.manager.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.command.DeviceCreateCommand;
import org.ouanu.manager.common.DeviceAuthenticationToken;
import org.ouanu.manager.dto.DeviceDto;
import org.ouanu.manager.exception.ConflictException;
import org.ouanu.manager.exception.DeviceNotFoundException;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.model.User;
import org.ouanu.manager.query.DeviceQuery;
import org.ouanu.manager.repository.DeviceRepository;
import org.ouanu.manager.repository.UserRepository;
import org.ouanu.manager.request.RegisterDeviceRequest;
import org.ouanu.manager.response.DeviceResponse;
import org.ouanu.manager.response.DeviceTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final EnhancedJwtService jwtService;

    public DeviceTokenResponse authenticate(String uuid, String signature) {
        if (!validateCredentials(uuid, signature)) {
            return new DeviceTokenResponse("");
        }
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device not found."));
        if (device != null) {
            DeviceAuthenticationToken auth = new DeviceAuthenticationToken(device, null);
            loginUpdateLastModifiedTime(uuid);
            return new DeviceTokenResponse(jwtService.generateToken(auth));
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
                throw new ConflictException("Failed to create the device.");
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void loginUpdateLastModifiedTime(String uuid) {
        deviceRepository.updateLoginTime(uuid, LocalDateTime.now());
    }

    public DeviceResponse findDevice(String uuid) throws DeviceNotFoundException {
        Device device = deviceRepository.findByUuid(uuid).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "设备未找到"));
        return DeviceResponse.fromEntity(device);
    }

    public List<DeviceDto> findByAdminConditions(DeviceQuery query) {
        List<Device> devices = findDevices(query);
        List<DeviceDto> list = new ArrayList<>();
        for (Device device : devices) {
            DeviceDto dto = DeviceDto.fromEntity(device);
            list.add(dto);
        }
        return list;
    }

    public List<DeviceResponse> findByConditions(DeviceQuery query) {
        List<Device> devices = findDevices(query);
        List<DeviceResponse> list = new ArrayList<>();
        for (Device device : devices) {
            DeviceResponse dto = DeviceResponse.fromEntity(device);
            list.add(dto);
        }
        return list;
    }

    private List<Device> findDevices(DeviceQuery query) {
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
        return entityManager.createQuery(cq).getResultList();
    }

    public boolean register(@Valid RegisterDeviceRequest request) {
        DeviceCreateCommand command = request.toCommand();
        return createDevice(command);
    }

    public boolean validateCredentials(String uuid, String signature) {
        return deviceRepository.findByUuid(uuid)
                .map(user -> Objects.equals(signature, user.getSignature()))
                .orElse(false);
    }

    public DeviceResponse getDeviceInfo() {
        try {
            Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) auth.getPrincipal();
            Device device = deviceRepository.findByUuid(principal.getUsername()).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device can not be found"));
            if (device.getSignature().equals(principal.getPassword())) {
                return DeviceResponse.fromEntity(device);
            }
            return null;
        } catch (Exception e) {
            System.out.println("DeviceService error = " + e.getMessage());
            return null;
        }
    }

    public DeviceDto updateDeviceAdmin(DeviceDto dto) {
        Optional<Device> optional = deviceRepository.findByUuid(dto.getUuid());
        if (optional.isEmpty()) {
            return null;
        }
        Device device = optional.get();
        String userUuid = dto.getUserUuid();
        Optional<User> optionalUser = userRepository.findByUuid(userUuid);
        optionalUser.ifPresent(device::setOwner);
        device.setLocked(dto.isLocked());
        device.setActive(dto.isActive());
        if (dto.getDeviceName() != null) {
            device.setDeviceName(dto.getDeviceName());
        }
        if (dto.getDeviceGroup() != null) {
            device.setDeviceGroup(dto.getDeviceGroup());
        }
        if (dto.getRemark() != null) {
            device.setRemark(dto.getRemark());
        }
        Device save = deviceRepository.save(device);
        return DeviceDto.fromEntity(save);
    }

    public Boolean deleteDeviceByAdmin(DeviceDto dto) {
        int i = deviceRepository.deleteByUuid(dto.getUuid());
        return i != 0;
    }

}
