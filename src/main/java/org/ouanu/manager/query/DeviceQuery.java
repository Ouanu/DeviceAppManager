package org.ouanu.manager.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeviceQuery {
    private String uuid;
    private String deviceName;
    private String deviceGroup;
    private Boolean active;
    private Boolean locked;
    private String userUuid;
    private TimeRange createTimeRange;
    private TimeRange lastModifiedTimeRange;

    @Data
    @AllArgsConstructor
    public static class TimeRange {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
