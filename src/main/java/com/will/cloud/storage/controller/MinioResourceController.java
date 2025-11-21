package com.will.cloud.storage.controller;

import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.MinioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource")
public class MinioResourceController {

    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<MinioResourceResponseDto> getResource(
            @Param("path") String path, @AuthenticationPrincipal User user) {
        log.info("User [{}] is trying to get resource [{}]", user.getUsername(), path);
        return ResponseEntity.ok().body(minioService.getResource(user, path));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@Param("path") String path) {
        minioService.deleteResource(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity downloadResource(@Param("path") String path) {
        // response should be in application/octet-stream format
        return null;
    }

    @GetMapping("/move")
    public ResponseEntity<MinioResourceResponseDto> moveResource(
            @Param("from") String from, @Param("to") String to) {
        return ResponseEntity.ok().body(minioService.moveResource(from, to));
    }

    @GetMapping("/search") // url encoded
    public ResponseEntity<List<MinioResourceResponseDto>> searchResource(
            @Param("query") String query) {
        return ResponseEntity.ok().body(minioService.search(query));
    }

    @PostMapping
    public ResponseEntity<List<MinioResourceResponseDto>> uploadResource(
            @Param("query") String query) {
        return ResponseEntity.status(HttpStatus.CREATED).body(minioService.upload(query));
    }
}
