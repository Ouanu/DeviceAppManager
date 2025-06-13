package org.ouanu.manager.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ouanu.manager.model.Application;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private String packageName;
    private String label;
    private String versionName;
    private Long versionCode;
    private String appNames;
    private LocalDateTime uploadTime;
    private Long size;
    private String fileName;

    public static ApplicationResponse fromEntity(Application application) {
        return ApplicationResponse.builder()
                .packageName(application.getPackageName())
                .label(application.getLabel())
                .versionName(application.getVersionName())
                .versionCode(application.getVersionCode())
                .appNames(application.getAppNames())
                .uploadTime(application.getUploadTime())
                .size(application.getSize())
                .fileName(application.getFileName())
                .build();
    }
}
