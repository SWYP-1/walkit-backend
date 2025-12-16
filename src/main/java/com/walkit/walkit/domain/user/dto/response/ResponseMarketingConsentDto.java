package com.walkit.walkit.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseMarketingConsentDto {

    private boolean isMarketingConsent;
}
