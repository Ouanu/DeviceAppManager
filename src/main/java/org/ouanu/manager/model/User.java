package org.ouanu.manager.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("is_locked = false")
@SQLDelete(sql = "UPDATE users SET is_locked = true WHERE id = ?")
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank(message = "UUID不能为空")
    private String uuid;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度需在6~20个字符之间")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\+?[0-9. ()-]{10,25}$", message = "请输入有效的手机号")
    private String phone;

    @Column
    @Email(message = "邮箱格式不正确")
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'CUSTOMER'")
    @Builder.Default
    private String role = "CUSTOMER";

    @Column(length = 200)
    @Size(max = 200, message = "备注信息不能超过200个字符")
    private String remark;

    @Column(name = "expire_date")
    private LocalDateTime expireDate; // 可添加过期时间段

    @Column(nullable = false, name = "is_locked", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean locked; // 账户是否未锁定

    @Column(name = "pwd_update_time")
    private LocalDateTime passwordUpdateTime; // 可添加密码强制修改时间

    @Column(nullable = false, name = "active", columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active; // 账户是否启用

    // 创建时间（由系统自动设置）
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    // 最后修改时间（包含活跃时间更新）
    @LastModifiedDate
    private LocalDateTime lastModifiedTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return expireDate == null || expireDate.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if ("ADMIN".equals(this.role)) {
            return true; // 管理员永不过期
        }
        return passwordUpdateTime == null || passwordUpdateTime.isAfter(LocalDateTime.now().minusYears(70));
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
