package com.linked.service;

import com.linked.dto.AuthDto;

public interface AuthService {

    AuthDto.TokenResponse login(AuthDto.LoginRequest request);

    AuthDto.CheckAvailabilityResponse checkAvailability(String field, String value);
}
