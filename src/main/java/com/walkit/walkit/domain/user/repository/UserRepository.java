package com.walkit.walkit.domain.user.repository;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.common.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    Optional<User> findByEmail(String email);

    boolean existsByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    Optional<User> findByNickname(String nickname);

    boolean existsByNickname(String nickname);
}
