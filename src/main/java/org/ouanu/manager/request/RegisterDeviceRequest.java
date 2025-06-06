package org.ouanu.manager.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.ouanu.manager.command.DeviceCreateCommand;
import org.ouanu.manager.command.UserCreateCommand;

public record RegisterDeviceRequest(
        @NotBlank String uuid,
        @NotBlank @Size(min = 3, max = 20) String deviceName,
        @NotBlank @Size(max = 20, message = "组名长度需在0~20个字符之间, 当为0时则为default_Group") String deviceGroup,
        @NotBlank String userUuid,
        @Column(length = 200) @Size(max = 200, message = "备注信息不能超过200个字符") String remark
) {
        public DeviceCreateCommand toCommand() {
                return new DeviceCreateCommand(uuid, deviceName, deviceGroup, userUuid, remark);
        }
}
