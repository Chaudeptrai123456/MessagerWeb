package com.example.Messenger.Repository;

import com.example.Messenger.Entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByEmail(String email);
    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.authorities a
        WHERE a.name = :roleName
    """)
    Page<User> findAllByRole(
            @Param("roleName") String roleName,
            Pageable pageable
    );

    Optional<User> findByEmail(String email);
}