package org.ouanu.manager.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserQuery {
    private String username;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
    private Boolean locked;
    private TimeRange createTimeRange;
    private TimeRange lastModifiedTimeRange;

    @Data
    @AllArgsConstructor
    public static class TimeRange {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
