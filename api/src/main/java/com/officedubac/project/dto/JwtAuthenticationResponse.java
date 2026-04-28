package com.officedubac.project.dto;

import lombok.Data;

@Data
public class JwtAuthenticationResponse
{
    private String token;
    private String refreshToken;
    private UserGoToFrontDTO user;
    private String sessionId;
}
