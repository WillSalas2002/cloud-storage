package com.will.cloud.storage.controller;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextLogoutHandler logoutHandler;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> singUp(@Validated @RequestBody AuthRequest request) {
        log.info("User {} is trying to sing up.", request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(
            @Validated @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        log.info("User {} is trying to sing in.", request.username());
        return ResponseEntity.status(HttpStatus.OK).body(authService.signIn(request, httpRequest));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<AuthResponse> signOut(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info(
                "User {} is trying to log out.",
                ((User) authentication.getPrincipal()).getUsername());
        this.logoutHandler.logout(request, response, authentication);
        log.info(
                "User {} successfully logged out.",
                ((User) authentication.getPrincipal()).getUsername());
        return ResponseEntity.noContent().build();
    }
}
