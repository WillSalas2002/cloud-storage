package com.will.cloud.storage.service;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse signUp(AuthRequest request);
}
