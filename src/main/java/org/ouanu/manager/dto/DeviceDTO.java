package org.ouanu.manager.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    private String uuid;
    private String deviceName;
    private String deviceGroup;
    private String model;
    private String brand;
    private String androidVersion;
    private LocalDateTime registrationTime;
    private LocalDateTime lastActiveTime;

}