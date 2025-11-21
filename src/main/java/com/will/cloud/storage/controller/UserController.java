package com.will.cloud.storage.controller;

import com.will.cloud.storage.dto.response.UserProfileDto;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/user/me")
    public UserProfileDto getProfile(Principal principal) {
        return new UserProfileDto(principal.getName());
    }
}
