package org.beaconfire.authentication.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserRole",
        uniqueConstraints = @UniqueConstraint(name = "unique_user_role", columnNames = {"UserID", "RoleID"}),
        indexes = {
                @Index(name = "idx_user_id", columnList = "UserID"),
                @Index(name = "idx_role_id", columnList = "RoleID")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, foreignKey = @ForeignKey(name = "FK_UserRole_User"))
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID", nullable = false, foreignKey = @ForeignKey(name = "FK_UserRole_Role"))
    private Role role;

    @Column(name = "ActiveFlag", nullable = false)
    @Builder.Default
    private Boolean activeFlag = true;

    @CreationTimestamp
    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "LastModificationDate", nullable = false)
    private LocalDateTime lastModificationDate;

    /**
     * Get the role name directly.
     * Useful for Spring Security integration.
     */
    public String getRoleName() {
        return role != null ? role.getRoleName() : null;
    }

    /**
     * Check if this user role is active and the role exists.
     */
    public boolean isActiveRole() {
        return activeFlag != null && activeFlag && role != null;
    }
}