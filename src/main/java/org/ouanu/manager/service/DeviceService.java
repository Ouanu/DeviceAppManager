package org.ouanu.manager.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ouanu.manager.command.DeviceCreateCommand;
import org.ouanu.manager.dto.DeviceDto;
import org.ouanu.manager.dto.UserDto;
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
import org.ouanu.manager.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final EntityManager entityManager;

    public DeviceTokenResponse authenticate(String uuid, String userUuid) {
        if (!deviceRepository.existsByUuid(uuid)) {
            return new DeviceTokenResponse("");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(uuid, userUuid);
        Authentication auth = authManager.authenticate(
                authenticationToken
        );
        loginUpdateLastModifiedTime(uuid);
        return new DeviceTokenResponse(jwtUtils.generateJwtToken(auth));
    }

    @Transactional
    public void createDevice(DeviceCreateCommand command) {
        User user = userRepository.findByUuid(command.userUuid()).orElseThrow(() -> new UsernameNotFoundException("用户未找到"));
        if (deviceRepository.existsByUuid(command.uuid())) {
            throw new ConflictException("设备已存在");
        }

        Device device = command.toEntity();
        device.setCreateTime(LocalDateTime.now());
        device.setLastModifiedTime(LocalDateTime.now());
        device.setOwner(user);
        device.setUserUuid(user.getUuid());
        deviceRepository.save(device);

        if (!deviceRepository.existsByUuid(device.getUuid())) {
            throw new ConflictException("设备创建失败");
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

    public void register(@Valid RegisterDeviceRequest request) {
        DeviceCreateCommand command = request.toCommand();
        createDevice(command);
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
