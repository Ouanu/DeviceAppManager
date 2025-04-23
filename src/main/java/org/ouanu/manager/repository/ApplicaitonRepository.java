package org.ouanu.manager.repository;

import org.ouanu.manager.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicaitonRepository extends JpaRepository<Application, Long> {

    // Find all applications by Device's Uuid.
    @Query("SELECT a FROM Application a WHERE a.device.uuid = :deviceUuid")
    List<Application> findByDeviceUuid(@Param("deviceUuid") String deviceUuid);

    // Find an application by Device's UUID and PackageName.
    @Query("SELECT a FROM Application a WHERE a.device.uuid = :deviceUuid AND a.packageName = :packageName")
    Optional<Application> findByDeviceUuidAndPackageName(
            @Param("deviceUuid") String deviceUuid,
            @Param("packageName") String packageName
    );

    // Count number of all applicaitons on the device via its UUID
    @Query("SELECT COUNT(a) FROM Application a WHERE a.device.uuid = :deviceUuid")
    Long countByDeviceUuid(@Param("deviceUuid") String deviceUuid);

    // Find all System Apps by Device's UUID.
    @Query("SELECT a FROM Application a WHERE a.device.uuid = :deviceUuid AND a.isSystemApp = true")
    List<Application> findSystemAppsByDeviceUuid(@Param("deviceUuid") String deviceUuid);

    // Find Non System Apps by Device's UUID.
    @Query("SELECT a FROM Application a WHERE a.device.uuid = :deviceUuid AND a.isSystemApp = false")
    List<Application> findNonSystemAppsByDeviceUuid(@Param("deviceUuid") String deviceUuid);

    // Warning!!
    // Remove all applicaitons on the device via its UUID.
    void deleteByDeviceUuid(String deviceUuid);

    // Search applicaitons by the device's UUID and app's Name.
    @Query("SELECT a FROM Application a WHERE a.device.uuid = :deviceUuid AND LOWER(a.appName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Application> searchByAppName(@Param("deviceUuid") String deviceUuid, @Param("query") String query);

}