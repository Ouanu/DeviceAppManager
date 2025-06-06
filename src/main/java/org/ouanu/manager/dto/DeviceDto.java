package org.ouanu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ouanu.manager.model.Device;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDto {
    private Long id;
    private String uuid;
    private String deviceName;
    private String deviceGroup;
    private boolean locked; // 设备是否锁定（对用户不可见）
    private boolean active; // 账户是否启用（用户可操作，用来禁用或启用设备）
    private LocalDateTime createTime;
    private LocalDateTime lastModifiedTime;
    private String remark;

    public static DeviceDto fromEntity(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .uuid(device.getUuid())
                .deviceName(device.getDeviceName())
                .deviceGroup(device.getDeviceGroup())
                .locked(device.isLocked())
                .active(device.isActive())
                .createTime(device.getCreateTime())
                .lastModifiedTime(device.getLastModifiedTime())
                .remark(device.getRemark())
                .build();
    }
}
