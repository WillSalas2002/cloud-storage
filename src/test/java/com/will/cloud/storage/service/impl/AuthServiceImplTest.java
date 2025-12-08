package com.will.cloud.storage.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.repository.UserRepository;
import com.will.cloud.storage.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

@Transactional
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthServiceImplTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired HttpServletRequest httpServletRequest;

    @Test
    void signUp_shouldCreateUserAndReturnAuthResponse() {
        AuthRequest authRequest = new AuthRequest("Will", "Salas");

        AuthResponse response = authService.signUp(authRequest, httpServletRequest);

        User saved = userRepository.findByUsername(authRequest.username()).orElse(null);

        assertNotNull(saved);
        assertNotEquals("Salas", saved.getPassword());
        assertTrue(passwordEncoder.matches("Salas", saved.getPassword()));
        assertEquals(saved.getUsername(), response.username());
    }
}
