package org.beaconfire.authentication.repository;

import org.beaconfire.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByActiveFlag(Boolean activeFlag);

    @Query("SELECT u FROM User u WHERE u.activeFlag = true")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.createDate >= :startDate AND u.createDate <= :endDate")
    List<User> findUsersByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.roleName = :roleName AND ur.activeFlag = true")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.activeFlag = true")
    long countActiveUsers();
}
