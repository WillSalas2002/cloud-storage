package com.will.cloud.storage.controller;

import static com.will.cloud.storage.util.AppConstants.MDC_USERNAME_KEY;

import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.MinioService;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource")
public class MinioResourceController {

    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<MinioResourceResponseDto> getResource(
            @RequestParam("path") String path, @AuthenticationPrincipal User user) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info("User [{}] is trying to get resource [{}]", MDC.get(MDC_USERNAME_KEY), path);

        return ResponseEntity.ok().body(minioService.getResource(user, path));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(
            @RequestParam("path") String path, @AuthenticationPrincipal User user) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info("User [{}] is trying to delete resource [{}]", MDC.get(MDC_USERNAME_KEY), path);

        minioService.deleteResource(user, path, true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Void> downloadResource(
            @RequestParam("path") String path,
            @AuthenticationPrincipal User user,
            HttpServletResponse response) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info("User [{}] is trying to download resource [{}]", MDC.get(MDC_USERNAME_KEY), path);

        minioService.downloadResource(path, user, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/move")
    public ResponseEntity<MinioResourceResponseDto> moveResource(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @AuthenticationPrincipal User user) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info(
                "User [{}] is trying to move resource from [{}] to [{}]",
                MDC.get(MDC_USERNAME_KEY),
                from,
                to);

        return ResponseEntity.ok().body(minioService.moveResource(from, to, user));
    }

    @GetMapping(value = "/search")
    public ResponseEntity<List<MinioResourceResponseDto>> searchResource(
            @RequestParam("query") String query, @AuthenticationPrincipal User user) {
        MDC.put(MDC_USERNAME_KEY, user.getUsername());
        log.info("User [{}] is searching for resource [{}]", MDC.get(MDC_USERNAME_KEY), query);
        return ResponseEntity.ok().body(minioService.search(query, user));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<MinioResourceResponseDto>> uploadResources(
            @RequestParam("path") String path,
            @RequestParam("object") MultipartFile[] files,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(minioService.upload(path, files, user));
    }
}
