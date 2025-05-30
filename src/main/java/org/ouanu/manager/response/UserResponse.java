package org.ouanu.manager.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ouanu.manager.dto.UserDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String uuid;
    private String username;
    private String phone;
    private String email;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime lastModifiedTime;

    public static UserResponse fromEntity(UserDto user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .remark(user.getRemark())
                .createTime(user.getCreateTime())
                .lastModifiedTime(user.getLastModifiedTime())
                .build();
    }
}
