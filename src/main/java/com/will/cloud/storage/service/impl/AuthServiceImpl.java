package com.will.cloud.storage.service.impl;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.mapper.AuthMapper;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.repository.UserRepository;
import com.will.cloud.storage.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signUp(AuthRequest request) {
        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        return authMapper.toAuthResponse(savedUser);
    }

    @Override
    public AuthResponse signIn(AuthRequest request, HttpServletRequest httpRequest) {
        Authentication authenticationToken =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication auth = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return new AuthResponse(auth.getName());
    }
}
