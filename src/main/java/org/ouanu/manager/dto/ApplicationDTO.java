package org.ouanu.manager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private String packageName;
    private String appName;
    private String appVersion;
    private Boolean isSystemApp;
    private Boolean isForceInstallation;
}
