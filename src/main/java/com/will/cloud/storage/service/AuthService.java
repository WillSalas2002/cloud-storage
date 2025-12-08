package com.will.cloud.storage.service;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse signUp(AuthRequest request, HttpServletRequest httpRequest);

    AuthResponse signIn(AuthRequest request, HttpServletRequest httpRequest);
}
