package org.ouanu.manager.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationQuery {
    private String packageName;
    private String label;
    private String appName;
    private TimeRange uploadTimeRange;
    private String banRegion;
    private String apkUrl;

    @Data
    @AllArgsConstructor
    public static class TimeRange {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
