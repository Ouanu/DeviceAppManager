package org.ouanu.manager.command;

import org.ouanu.manager.model.Device;

public record DeviceCreateCommand(
        String uuid,
        String deviceName,
        String deviceGroup,
        String userUuid,
        String remark
) {

    @Override
    public String uuid() {
        return uuid;
    }

    @Override
    public String deviceName() {
        return deviceName;
    }

    @Override
    public String deviceGroup() {
        return deviceGroup;
    }

    @Override
    public String userUuid() {
        return userUuid;
    }

    public String remark() {
        return remark;
    }

    public Device toEntity() {
        return Device.builder()
                .uuid(uuid)
                .deviceName(deviceName)
                .deviceGroup(deviceGroup)
                .userUuid(userUuid)
                .remark(remark)
                .build();
    }
}
