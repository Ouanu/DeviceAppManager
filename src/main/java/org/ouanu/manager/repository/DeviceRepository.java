package org.ouanu.manager.repository;

import org.ouanu.manager.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    // Find the device by Device's UUID.
    Optional<Device> findByUuid(String uuid);

    // Check if the device exists.
    boolean existsByUuid(String uuid);

    // Find all devices by User's UUID.
    @Query("SELECT d FROM Device d WHERE d.user.uuid = :userUuid")
    List<Device> findByUserUuid(@Param("userUuid") String userUuid);

    // Count the number of all devices for the user.
    @Query("SELECT COUNT(d) FROM Device d WHERE d.user.uuid = :userUuid")
    Long countByUserUuid(@Param("userUuid") String userUuid);

    // Warning!!
    // Delete all devices of the user(userUuid).
    void deleteByUserUuid(String userUuid);

}