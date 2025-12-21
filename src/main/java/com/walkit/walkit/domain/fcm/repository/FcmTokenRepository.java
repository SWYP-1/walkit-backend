package com.walkit.walkit.domain.fcm.repository;

import com.walkit.walkit.domain.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findTopByUserIdAndEnabledTrueOrderByLastUsedAtDesc(Long userId);

    void deleteByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update FcmToken t set t.enabled = false where t.user.id = :userId and t.enabled = true")
    int disableAllActiveByUserId(@Param("userId") Long userId);

    List<FcmToken> findByUserIdAndEnabled(Long userId, Boolean enabled);

    // 가장 최근 토큰 1개
    Optional<FcmToken> findTop1ByUserIdOrderByModifiedDateDesc(Long userId);
}


