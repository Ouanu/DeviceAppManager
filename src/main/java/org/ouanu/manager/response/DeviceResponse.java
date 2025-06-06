package org.ouanu.manager.response;

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
public class DeviceResponse {
    private String uuid;
    private String deviceName;
    private String deviceGroup;
    private boolean active;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime lastModifiedTime;
    private String userUuid;

    public static DeviceResponse fromEntity(Device device) {
        return DeviceResponse.builder()
                .uuid(device.getUuid())
                .deviceName(device.getDeviceName())
                .deviceGroup(device.getDeviceGroup())
                .active(device.isActive())
                .remark(device.getRemark())
                .createTime(device.getCreateTime())
                .lastModifiedTime(device.getLastModifiedTime())
                .userUuid(device.getUserUuid())
                .build();
    }
}
