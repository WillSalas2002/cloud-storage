package com.will.cloud.storage.controller;

import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.MinioService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.will.cloud.storage.util.AppConstants.MDC_USERNAME_KEY;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directory")
public class MinioDirectoryController {

    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<List<MinioResourceResponseDto>> getDirectory(
            @RequestParam(value = "path") String path, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(minioService.search(path, user));
    }

    @PostMapping
    public ResponseEntity<MinioResourceResponseDto> createDirectory(
            @RequestParam("path") String path, @AuthenticationPrincipal User user) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info("User [{}] is trying to create a directory [{}]", MDC.get(MDC_USERNAME_KEY), path);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(minioService.createDirectory(user, path));
    }
}
