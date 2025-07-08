package org.beaconfire.authentication.repository;

import org.beaconfire.authentication.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    List<UserRole> findByUserId(Integer userId);

    List<UserRole> findByRoleId(Integer roleId);

    List<UserRole> findByUserIdAndActiveFlag(Integer userId, Boolean activeFlag);

    List<UserRole> findByRoleIdAndActiveFlag(Integer roleId, Boolean activeFlag);

    Optional<UserRole> findByUserIdAndRoleId(Integer userId, Integer roleId);

    boolean existsByUserIdAndRoleId(Integer userId, Integer roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId AND ur.activeFlag = true")
    Optional<UserRole> findActiveUserRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    @Modifying
    @Transactional
    @Query("UPDATE UserRole ur SET ur.activeFlag = :activeFlag WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    int updateUserRoleActiveFlag(@Param("userId") Integer userId,
                                 @Param("roleId") Integer roleId,
                                 @Param("activeFlag") Boolean activeFlag);

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.id = :userId AND ur.activeFlag = true")
    long countActiveRolesByUserId(@Param("userId") Integer userId);
}