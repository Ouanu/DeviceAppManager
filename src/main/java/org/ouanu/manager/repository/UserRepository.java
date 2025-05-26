package org.ouanu.manager.repository;

import org.ouanu.manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(String uuid);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
//    List<User> findAll();

    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByUuid(String uuid);



}
