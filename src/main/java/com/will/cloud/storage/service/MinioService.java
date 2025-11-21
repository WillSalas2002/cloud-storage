package com.will.cloud.storage.service;

import com.will.cloud.storage.dto.response.MinioCreateDirectoryResponseDto;
import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.model.User;

import java.util.List;

public interface MinioService {

    MinioResourceResponseDto getResource(User user, String path);

    void deleteResource(String path);

    MinioResourceResponseDto moveResource(String from, String to);

    List<MinioResourceResponseDto> search(String query);

    List<MinioResourceResponseDto> upload(String query);

    MinioCreateDirectoryResponseDto createDirectory(String path);
}
