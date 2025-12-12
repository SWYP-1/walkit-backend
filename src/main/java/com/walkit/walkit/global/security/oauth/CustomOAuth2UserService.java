package com.walkit.walkit.global.security.oauth;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> attributes = oauth2User.getAttributes();


        OAuthAttributes oAuthAttributes = OAuthAttributes.of(
            registrationId,
            userNameAttributeName,
            attributes
        );

        User user = saveOrUpdate(oAuthAttributes);

        return CustomOAuth2User.create(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            attributes,
            oAuthAttributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByAuthProviderAndProviderId(
                attributes.getAuthProvider(),
                attributes.getOauth2UserInfo().getProviderId()
        ).map(existingUser -> existingUser.update(
                attributes.getOauth2UserInfo().getName(),
                attributes.getOauth2UserInfo().getProfileImageUrl()
        )).orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
