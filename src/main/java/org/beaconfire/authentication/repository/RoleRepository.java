package org.beaconfire.authentication.repository;

import org.beaconfire.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    @Query("SELECT r FROM Role r ORDER BY r.roleName ASC")
    List<Role> findAllOrderByRoleName();

    @Query("SELECT r FROM Role r WHERE r.roleDescription LIKE %:keyword%")
    List<Role> findByRoleDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT r FROM Role r JOIN r.userRoles ur WHERE ur.user.id = :userId AND ur.activeFlag = true")
    List<Role> findRolesByUserId(@Param("userId") Integer userId);
}