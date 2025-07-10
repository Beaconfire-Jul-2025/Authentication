package org.beaconfire.authentication.repository;

import org.beaconfire.authentication.model.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, Integer> {

    Optional<RegistrationToken> findByToken(String token);

    Optional<RegistrationToken> findByEmail(String email);

    List<RegistrationToken> findByCreatedById(Integer createdById);

    @Query("SELECT rt FROM RegistrationToken rt WHERE rt.expirationDate > :currentDate")
    List<RegistrationToken> findValidTokens(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT rt FROM RegistrationToken rt WHERE rt.expirationDate <= :currentDate")
    List<RegistrationToken> findExpiredTokens(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT rt FROM RegistrationToken rt WHERE rt.token = :token AND rt.expirationDate > :currentDate")
    Optional<RegistrationToken> findValidTokenByToken(@Param("token") String token,
                                                      @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT rt FROM RegistrationToken rt WHERE rt.email = :email AND rt.expirationDate > :currentTime")
    Optional<RegistrationToken> findValidTokenByEmail(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM RegistrationToken rt WHERE rt.expirationDate <= :currentDate")
    int deleteExpiredTokens(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT COUNT(rt) FROM RegistrationToken rt WHERE rt.createdBy.id = :userId")
    long countTokensByCreatedBy(@Param("userId") Integer userId);

    boolean existsByToken(String token);
}