package com.walkit.walkit.domain.fcm.dto;

import com.walkit.walkit.domain.fcm.entity.DeviceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRegisterRequestDto {
    private String token;
    private DeviceType deviceType;
    private String deviceId;
}
