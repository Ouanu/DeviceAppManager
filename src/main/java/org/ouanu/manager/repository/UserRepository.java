package org.ouanu.manager.repository;

import org.ouanu.manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find an user by the User's UUID
    Optional<User> findByUuid(String uuid);

    // Find an user by the username.
    Optional<User> findByUsername(String username);

    // Find an user by the phone number.
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Check if the user exists by the username.
    boolean existsByUsername(String username);

    // Check if the user exists by the phone number.
    boolean existsByPhoneNumber(String phoneNumber);

    // Check if the user exists by the User's UUID.
    boolean existsByUuid(String uuid);

    // Find username of the user by the phone number.(Used for login.)
    String findUsernameByPhoneNumber(String phoneNumber);
}