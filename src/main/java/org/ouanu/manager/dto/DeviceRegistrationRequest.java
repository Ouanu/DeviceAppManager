package org.ouanu.manager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {
    private String userUuid;
    private String deviceUuid;
    private String deviceName;
    private String model;
    private String brand;
    private String imei;
    private String androidVersion;
}