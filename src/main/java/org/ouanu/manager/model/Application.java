package org.ouanu.manager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_uuid", referencedColumnName = "uuid", nullable = false)
    private Device device;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private String appName;

    private String appVersion;
    private Boolean isSystemApp = false; // Default false
    private Boolean isForceInstallation = false; // Optional installation by default
}