package org.ouanu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRegistrationRequest {
    private String deviceUuid;
    private String packageName;
    private String appName;

    private String appVersion;
    private Boolean isSystemApp = false; // Default false
    private Boolean isForceInstallation = false; // Optional installation by default
}