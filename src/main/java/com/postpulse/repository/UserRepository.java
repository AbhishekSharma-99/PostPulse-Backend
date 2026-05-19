package com.postpulse.repository;

import com.postpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmailWithRoles(@Param("username") String username, @Param("email") String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
