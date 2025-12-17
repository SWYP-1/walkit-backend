package com.walkit.walkit.domain.user.repository;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.common.enums.AuthProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    Optional<User> findByEmail(String email);

    boolean existsByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    Optional<User> findByNickname(String nickname);


    @Query("select u.lastAccessAt from User u where u.id = :userId")
    LocalDateTime findLastAccessAt(@Param("userId") Long userId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.lastAccessAt = :now where u.id = :userId")
    int updateLastAccessAt(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // 유저의 48시간 이상 미접속인지 판단
    @Query("""
    select u
    from User u
    where u.lastAccessAt is not null
      and u.lastAccessAt <= :threshold
      and (u.inactive48hNotifiedAt is null or u.inactive48hNotifiedAt < u.lastAccessAt)
    """)
    List<User> findInactive48hTargets(@Param("threshold") LocalDateTime threshold,
                                      Pageable pageable);

    // 유저한테 알림 기록 - 중복 발송 방지
    @Modifying
    @Query("""
    update User u
       set u.inactive48hNotifiedAt = :now
     where u.id in :userIds
    """)
    void markInactive48hNotified(@Param("userIds") List<Long> userIds,
                                 @Param("now") LocalDateTime now);


    boolean existsByNickname(String nickname);

}
