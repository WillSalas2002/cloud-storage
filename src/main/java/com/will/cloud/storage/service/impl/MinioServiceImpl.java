package com.will.cloud.storage.service.impl;

import com.will.cloud.storage.dto.response.MinioCreateDirectoryResponseDto;
import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.exception.ResourceNotFoundException;
import com.will.cloud.storage.mapper.ItemMapper;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.MinioService;
import com.will.cloud.storage.service.MinioUtils;
import com.will.cloud.storage.util.AppConstants;

import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioUtils minioUtils;
    private final ItemMapper itemMapper;

    @Override
    public MinioResourceResponseDto getResource(User user, String path) {
        log.info("User: [{}], remaking path", user.getUsername());
        boolean isFolder = path.endsWith("/");
        String actualPath = remakePath(path, user.getId(), isFolder);

        log.info("User: [{}], checking if file exists under path [{}]", user.getUsername(), path);
        if (!isResourceExist(actualPath, isFolder)) {
            throw new ResourceNotFoundException(
                    path, String.format("Resource [%s] cannot be found", path));
        }

        Item item = minioUtils.getObjectByPath(AppConstants.BUCKET_NAME, actualPath);
        log.info("User: [{}], found and returning requested file.", user.getUsername());
        return itemMapper.mapToMinioResourceResponseDto(item);
    }

    @Override
    public void deleteResource(String path) {}

    @Override
    public MinioResourceResponseDto moveResource(String from, String to) {
        return null;
    }

    @Override
    public List<MinioResourceResponseDto> search(String query) {
        return List.of();
    }

    @Override
    public List<MinioResourceResponseDto> upload(String query) {
        return List.of();
    }

    @Override
    public MinioCreateDirectoryResponseDto createDirectory(String path) {
        return null;
    }

    private String remakePath(String path, Long id, boolean isFolder) {
        String prefix = String.format(AppConstants.PERSONAL_FOLDER_NAME_TEMPLATE, id);
        return isFolder ? prefix.concat(path.substring(0, path.length() - 1)) : prefix.concat(path);
    }

    private boolean isResourceExist(String path, boolean isFolder) {
        boolean isResourceExist;

        if (isFolder) {
            isResourceExist = minioUtils.isFolderExist(AppConstants.BUCKET_NAME, path);
        } else {
            isResourceExist = minioUtils.isObjectExist(AppConstants.BUCKET_NAME, path);
        }

        return isResourceExist;
    }
}
