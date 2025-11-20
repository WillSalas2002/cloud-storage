package com.will.cloud.storage.controller;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextLogoutHandler logoutHandler;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> singUp(@Validated @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(
            @Validated @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.signIn(request, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<AuthResponse> signOut(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        this.logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
