package com.will.cloud.storage.service;

import com.will.cloud.storage.dto.response.MinioCreateDirectoryResponseDto;
import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.model.User;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioService {

    MinioResourceResponseDto getResource(User user, String path);

    void deleteResource(User user, String path);

    MinioResourceResponseDto moveResource(String from, String to);

    List<MinioResourceResponseDto> search(String query);

    List<MinioResourceResponseDto> upload(String query, MultipartFile file);

    MinioCreateDirectoryResponseDto createDirectory(String path);
}
