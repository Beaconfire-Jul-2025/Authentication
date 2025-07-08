package org.beaconfire.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "RegistrationToken", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_expiration", columnList = "expirationDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Token", nullable = false, unique = true)
    @NotBlank(message = "Token is required")
    @Size(max = 255, message = "Token must not exceed 255 characters")
    private String token;

    @Column(name = "Email", nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Column(name = "ExpirationDate", nullable = false)
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expirationDate;

    @CreationTimestamp
    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreateBy", foreignKey = @ForeignKey(name = "FK_RegistrationToken_User"))
    private User createdBy;
}