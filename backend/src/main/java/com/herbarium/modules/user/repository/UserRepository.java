package com.herbarium.modules.user.repository;

import com.herbarium.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByUsernameContainingAndRealNameContaining(String username, String realName, Pageable pageable);

    Page<User> findByUsernameContaining(String username, Pageable pageable);

    Page<User> findByRealNameContaining(String realName, Pageable pageable);
}
