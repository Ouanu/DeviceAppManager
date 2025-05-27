package org.ouanu.manager.repository;

import org.ouanu.manager.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUuid(String uuid);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    //    List<User> findAll();
    @Modifying
    @Query("UPDATE User u SET u.lastModifiedTime = :time WHERE u.username = :username")
    void updateLoginTime(@Param("username") String username,
                         @Param("time") LocalDateTime time);
    // 获取用户完整信息用于认证
    @EntityGraph(attributePaths = {"authorities"})
    Optional<User> findWithAuthoritiesByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUuid(String uuid);


}
