package org.ouanu.manager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid; // The random UUID of the device.

    @ManyToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", nullable = false)
    private User user;

    private String deviceName;
    private String deviceGroup; // This is what clients use to create their own groups. Something like different regions......
    private String model;
    private String brand;
    private String imei;
    private String androidVersion;

    @Column(nullable = false)
    private LocalDateTime registrationTime;

    private LocalDateTime lastActiveTime;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications;


}