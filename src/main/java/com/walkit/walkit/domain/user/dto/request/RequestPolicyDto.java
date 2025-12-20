package com.walkit.walkit.domain.user.dto.request;

import lombok.Getter;

@Getter
public class RequestPolicyDto {

    private boolean termsAgreed;
    private boolean privacyAgreed;
    private boolean locationAgreed;
    private boolean marketingConsent;
}
