package com.will.cloud.storage.service.impl;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.mapper.AuthMapper;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.repository.UserRepository;
import com.will.cloud.storage.service.AuthService;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final UserRepository userRepository;

    @Override
    public AuthResponse signUp(AuthRequest request) {
        User user = userRepository.save(authMapper.toUser(request));
        return authMapper.toAuthResponse(user);
    }
}
