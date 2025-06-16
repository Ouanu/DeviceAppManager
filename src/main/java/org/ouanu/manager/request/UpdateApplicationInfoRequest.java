package org.ouanu.manager.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationInfoRequest(
        @NotBlank
        String packageName,
        String label,
        String versionName,
        String versionCode,
        String appNames,
        Long size,
        @NotBlank
        String fileName
) {

}
