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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank(message = "UUID不能为空")
    private String uuid;

    @Column(nullable = false)
    @NotBlank(message = "设备名不能为空")
    @Size(min = 6, max = 20, message = "设备名长度需在6~20个字符之间")
    private String deviceName;

    @Column(nullable = false)
    @Size(max = 20, message = "组名长度需在0~20个字符之间, 当为0时则为default_Group")
    @Builder.Default
    private String deviceGroup = "default_group";

    @Column(nullable = false, name = "is_locked")
    @Builder.Default
    private boolean locked = false; // 设备是否锁定（对用户不可见）

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private boolean active = true; // 账户是否启用（用户可操作，用来禁用或启用设备）

    // 创建时间（由系统自动设置）
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    // 最后修改时间（包含活跃时间更新）
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedTime;

    @Column(length = 200)
    @Size(max = 200, message = "备注信息不能超过200个字符")
    private String remark;

    @Column(nullable = false)
    @NotBlank(message = "所属用户UUID不能为空")
    private String userUuid;

    @ManyToOne(fetch = FetchType.LAZY)  // 多设备属于一个用户
    @JoinColumn(name = "user_id")       // 实际存储的是 users.id
    private User owner;

}
