package org.ouanu.manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("is_locked = false") // 替代@Where
@SQLDelete(sql = "UPDATE users SET is_locked = true WHERE id = ?") // 软删除SQL
@Data
@Builder
@Table(name = "devices")
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue
    private Long deviceId;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank(message = "UUID不能为空")
    private String deviceUuid;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "设备名不能为空")
    @Size(min = 6, max = 20, message = "设备名长度需在6~20个字符之间")
    private String deviceName;

    @Column(nullable = false, name = "Default Group")
    @Size(max = 20, message = "组名长度需在0~20个字符之间, 当为0时则为Default Group")
    private String deviceGroup;

    @Column(name = "is_locked")
    @Builder.Default
    private boolean locked = false; // 设备是否锁定（对用户不可见）

    @Column(name = "active")
    @Builder.Default
    private boolean active = true; // 账户是否启用（用户可操作，用来禁用或启用设备）

}
