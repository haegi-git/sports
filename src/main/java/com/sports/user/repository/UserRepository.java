package com.sports.user.repository;

import com.sports.user.entito.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByProviderId(String providerId);

    // email기준 검색
    @Query(value = "SELECT * FROM User WHERE email LIKE %:keyword%", nativeQuery = true)
    List<User> searchByEmail(String keyword);

}