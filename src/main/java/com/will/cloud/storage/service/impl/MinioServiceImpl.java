package com.will.cloud.storage.service.impl;

import static com.will.cloud.storage.util.AppConstants.MDC_USERNAME_KEY;

import com.will.cloud.storage.dto.response.MinioCreateDirectoryResponseDto;
import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.exception.ResourceNotFoundException;
import com.will.cloud.storage.mapper.ItemMapper;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.service.MinioService;
import com.will.cloud.storage.service.MinioUtils;
import com.will.cloud.storage.util.AppConstants;

import io.minio.GenericResponse;
import io.minio.Result;
import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioUtils minioUtils;
    private final ItemMapper itemMapper;

    @Override
    public MinioResourceResponseDto getResource(User user, String path) {
        String actualPath = remakePath(path, user);
        checkResourceExistsOrThrowException(actualPath, isFolder(path));

        Item item = minioUtils.getObjectByPath(AppConstants.BUCKET_NAME, actualPath);
        log.info("User: [{}], found and returning requested file.", user.getUsername());
        return itemMapper.mapToMinioResourceResponseDto(item);
    }

    @Override
    public void deleteResource(User user, String path) {
        String actualPath = remakePath(path, user);
        checkResourceExistsOrThrowException(actualPath, isFolder(path));

        if (isFolder(path)) {
            removeFilesFromFolderRecursively(actualPath);
        } else {
            minioUtils.removeFile(AppConstants.BUCKET_NAME, actualPath);
        }
        log.info(
                "User: [{}], found and removed requested [{}] file/folder.",
                MDC.get(MDC_USERNAME_KEY),
                actualPath);

        restoreFolder(path.substring(0, path.lastIndexOf("/") + 1), user);
    }

    @Override
    public MinioResourceResponseDto moveResource(String from, String to) {
        return null;
    }

    @Override
    public List<MinioResourceResponseDto> search(String query) {
        return List.of();
    }

    @Override
    public List<MinioResourceResponseDto> upload(String path, MultipartFile file) {
        // TODO: will be reworked
        String fileName = path + file.getOriginalFilename();
        GenericResponse objectWriteResponse =
                minioUtils.uploadFile(
                        AppConstants.BUCKET_NAME,
                        file,
                        fileName,
                        MediaType.MULTIPART_FORM_DATA_VALUE);
        return List.of();
    }

    @Override
    public MinioCreateDirectoryResponseDto createDirectory(String path) {
        return null;
    }

    private void restoreFolder(String folderToBeRestored, User user) {
        log.info("User [{}], restoring folder [{}]", MDC.get(MDC_USERNAME_KEY), folderToBeRestored);
        String personalFolder =
                String.format(AppConstants.PERSONAL_FOLDER_NAME_TEMPLATE, user.getId());
        minioUtils.createDir(AppConstants.BUCKET_NAME, personalFolder.concat(folderToBeRestored));
    }

    private void removeFilesFromFolderRecursively(String actualPath) {
        log.info(
                "User [{}], the specified resource is folder, trying to remove it with all the files inside.",
                MDC.get(MDC_USERNAME_KEY));
        Iterable<Result<Item>> results =
                minioUtils.listObjects(AppConstants.BUCKET_NAME, actualPath, true);
        results.forEach(
                i -> {
                    String fullFileName = null;
                    try {
                        fullFileName = i.get().objectName();
                        minioUtils.removeFile(AppConstants.BUCKET_NAME, fullFileName);
                    } catch (Exception e) {
                        log.warn(
                                "Error happened when trying to remove the file [{}]", fullFileName);
                    }
                });
    }

    private String remakePath(String path, User user) {
        log.info("User: [{}], remaking path", MDC.get(MDC_USERNAME_KEY));
        String prefix = String.format(AppConstants.PERSONAL_FOLDER_NAME_TEMPLATE, user.getId());
        return isFolder(path)
                ? prefix.concat(path.substring(0, path.length() - 1))
                : prefix.concat(path);
    }

    private void checkResourceExistsOrThrowException(String path, boolean isFolder) {
        log.info(
                "User: [{}], checking if file exists under path [{}]",
                MDC.get(MDC_USERNAME_KEY),
                path);
        boolean isResourceExist;

        if (isFolder) {
            isResourceExist = minioUtils.isFolderExist(AppConstants.BUCKET_NAME, path);
        } else {
            isResourceExist = minioUtils.isObjectExist(AppConstants.BUCKET_NAME, path);
        }

        if (!isResourceExist) {
            throw new ResourceNotFoundException(
                    path, String.format("Resource [%s] cannot be found", path));
        }
    }

    private boolean isFolder(String path) {
        return path.endsWith("/");
    }
}
