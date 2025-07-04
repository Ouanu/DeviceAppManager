package org.ouanu.manager.repository;

import org.ouanu.manager.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUuid(String uuid);
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.lastModifiedTime = :time WHERE d.uuid = :uuid")
    void updateLoginTime(@Param("uuid") String uuid, @Param("time") LocalDateTime time);
    boolean existsByUuid(String uuid);
    @Modifying
    int deleteByUuid(String uuid);

    @Modifying
    @Query("DELETE FROM User u WHERE u.uuid = :uuid")
    int hardDeleteByUuid(@Param("uuid") String uuid);
}
