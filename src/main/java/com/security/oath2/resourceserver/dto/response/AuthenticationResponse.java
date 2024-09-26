package com.security.oath2.resourceserver.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private Instant tokenExpirationTime;
    private String refreshToken;
    private Instant refreshTokenExpirationTime;
}
