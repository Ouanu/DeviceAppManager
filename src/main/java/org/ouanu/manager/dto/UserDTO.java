package org.ouanu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ouanu.manager.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String uuid;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String role;
    private String remark;
    private LocalDateTime expireDate;
    private boolean locked;
    private boolean active;
    private LocalDateTime createTime;
    private LocalDateTime lastModifiedTime;

    // 可以添加一些转换方法
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .username(user.getUsername())
                .password(user.getPassword())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .remark(user.getRemark())
                .expireDate(user.getExpireDate())
                .locked(user.isLocked())
                .active(user.isEnabled())
                .createTime(user.getCreateTime())
                .lastModifiedTime(user.getLastModifiedTime())
                .build();
    }
}
