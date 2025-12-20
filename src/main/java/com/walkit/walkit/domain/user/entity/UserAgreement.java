package com.walkit.walkit.domain.user.entity;

import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserAgreement {

    private Boolean isTermAgreed = false;
    private Boolean isPrivacyAgreed = false;
    private Boolean isLocationAgreed = false;

    public void update(RequestPolicyDto dto) {
        this.isTermAgreed = dto.isTermsAgreed();
        this.isPrivacyAgreed = dto.isPrivacyAgreed();
        this.isLocationAgreed = dto.isLocationAgreed();
    }
}
