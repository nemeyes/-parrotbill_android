package com.seerstech.chat.client.jwt;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class JWTToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long refreshTokenExpTime;
    private String userId;
    private String userNickname;
    private String userRole;
}
