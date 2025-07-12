package org.beaconfire.authentication.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserRole",
        uniqueConstraints = @UniqueConstraint(name = "unique_user_role", columnNames = {"userId", "roleId"}),
        indexes = {
                @Index(name = "idx_user_id", columnList = "userId"),
                @Index(name = "idx_role_id", columnList = "roleId")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false, foreignKey = @ForeignKey(name = "FK_UserRole_User"))
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId", nullable = false, foreignKey = @ForeignKey(name = "FK_UserRole_Role"))
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activeFlag = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(nullable = false)
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