package de.mainwetten.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select user
        from AppUser user
        where user.id = :userId
        """)
    Optional<AppUser> findByIdForUpdate(
            @Param("userId") Long userId
    );

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}