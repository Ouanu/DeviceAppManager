package org.ouanu.manager.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.ouanu.manager.response.ApplicationResponse;

@Builder
public record ApplicationInfoRequest(
        @NotBlank
        String packageName,
        String label,
        String versionName,
        Long versionCode,
        String appNames,
        Long size,
        @NotBlank
        String fileName,
        @NotBlank
        String iconName
) {
        public static ApplicationInfoRequest fromEntity(ApplicationResponse application) {
                return ApplicationInfoRequest.builder()
                        .packageName(application.getPackageName())
                        .label(application.getLabel())
                        .versionName(application.getVersionName())
                        .versionCode(application.getVersionCode())
                        .appNames(application.getAppNames())
                        .size(application.getSize())
                        .fileName(application.getFileName())
                        .iconName(application.getIconName())
                        .build();
        }
}
