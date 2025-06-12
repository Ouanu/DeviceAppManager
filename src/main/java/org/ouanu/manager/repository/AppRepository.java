package org.ouanu.manager.repository;

import org.ouanu.manager.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AppRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByPackageName(String packageName);

    @Modifying
    @Transactional
    @Query("UPDATE Application a SET a.uploadTime = :uploadTime WHERE a.packageName = :packageName")
    void updateLoginTime(@Param("packageName") String packageName, @Param("uploadTime") LocalDateTime uploadTime);

    boolean existsByPackageName(String packageName);
}
