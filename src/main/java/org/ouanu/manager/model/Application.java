package org.ouanu.manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "apps")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @NotBlank(message = "Package Name cannot be null")
    private String packageName;

    @Column(nullable = false)
    @NotBlank(message = "App Label cannot be null")
    private String label;

    @Column(nullable = false)
    @NotBlank(message = "Version Name cannot be null")
    private String versionName;

    @Column(nullable = false)
    private Long versionCode;

    @Column(nullable = false)
    private String appNames;

    @Column(nullable = false)
    private LocalDateTime uploadTime; // The time of application upload.

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    @Builder.Default
    private String banRegions = ""; // Restricted area for not displaying applications

    @Column(nullable = false)
    private String fileName; // Download the apk url.


}
