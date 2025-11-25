package com.will.cloud.storage.service.impl;

import static com.will.cloud.storage.util.AppConstants.MDC_USERNAME_KEY;
import static com.will.cloud.storage.util.AppConstants.SIGN_SLASH;

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

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public void deleteResource(User user, String path, boolean isRestoreFolder) {
        String actualPath = remakePath(path, user);
        checkResourceExistsOrThrowException(actualPath, isFolder(path));

        if (isFolder(path)) {
            removeFilesFromFolderRecursively(actualPath);
        } else {
            minioUtils.removeFile(AppConstants.BUCKET_NAME, actualPath);
        }
        log.info(
                "User: [{}], found and removed resource [{}] file/folder.",
                MDC.get(MDC_USERNAME_KEY),
                actualPath);

        if (isRestoreFolder) {
            restoreFolder(path.substring(0, path.lastIndexOf(SIGN_SLASH) + 1), user);
        }
    }

    @Override
    public void downloadResource(String path, User user, HttpServletResponse response) {
        String actualPath = remakePath(path, user);
        boolean isFolder = isFolder(path);
        checkResourceExistsOrThrowException(actualPath, isFolder);

        if (isFolder) {
            downloadFolderAsZip(actualPath, response);
        } else {
            downloadSingleFile(actualPath, response);
        }
    }

    @Override
    public MinioResourceResponseDto moveResource(String from, String to, User user) {
        String actualFromPath = remakePath(from, user);
        checkResourceExistsOrThrowException(actualFromPath, isFolder(from));
        String actualToPath = remakePath(to, user);

        moveResource(actualFromPath, actualToPath, isFolder(from));

        deleteResource(user, from, false);
        return itemMapper.mapToMinioResourceResponseDto(
                minioUtils.getObjectByPath(AppConstants.BUCKET_NAME, actualToPath));
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

    private void moveResource(String actualFromPath, String actualToPath, boolean isFolder) {
        Iterable<Result<Item>> results =
                minioUtils.listObjects(AppConstants.BUCKET_NAME, actualFromPath, true);
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                String from = item.objectName();
                String to;

                if (!isFolder) {
                    to =
                            actualToPath.concat(
                                    actualFromPath.substring(
                                            actualFromPath.lastIndexOf(SIGN_SLASH)));
                    log.info(
                            "User [{}], moving file from [{}] to [{}]",
                            MDC.get(MDC_USERNAME_KEY),
                            from,
                            to);
                    minioUtils.copyFile(
                            AppConstants.BUCKET_NAME, from, AppConstants.BUCKET_NAME, to);
                    break;
                }

                to = actualToPath.concat(from.substring(actualFromPath.length()));
                log.info(
                        "User [{}], moving file from [{}] to [{}]",
                        MDC.get(MDC_USERNAME_KEY),
                        from,
                        to);
                minioUtils.copyFile(AppConstants.BUCKET_NAME, from, AppConstants.BUCKET_NAME, to);
            } catch (Exception e) {
                log.error(
                        "User: [{}], error occurred when trying to move file [{}] to [{}]",
                        MDC.get(MDC_USERNAME_KEY),
                        actualFromPath,
                        actualToPath);
            }
        }
    }

    private void downloadSingleFile(String objectName, HttpServletResponse response) {
        setHeaders(objectName, response);
        log.info(
                "User: [{}], getting file's [{}] inputStream and writing it to response",
                MDC.get(MDC_USERNAME_KEY),
                objectName);

        try (InputStream is = minioUtils.getObject(AppConstants.BUCKET_NAME, objectName);
                OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
            os.flush();
        } catch (Exception e) {
            log.error(
                    "User: [{}], error occurred when downloading resource [{}]",
                    MDC.get(MDC_USERNAME_KEY),
                    objectName);
        }
    }

    private void downloadFolderAsZip(String folderPath, HttpServletResponse response) {
        setHeaders(folderPath, response);
        log.info(
                "User: [{}], getting folder's [{}] and its contents inputStream and writing it to response",
                MDC.get(MDC_USERNAME_KEY),
                folderPath);

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            Iterable<Result<Item>> results =
                    minioUtils.listObjects(AppConstants.BUCKET_NAME, folderPath, true);

            for (Result<Item> r : results) {
                Item item = r.get();
                if (item.isDir()) continue;

                try (InputStream is =
                        minioUtils.getObject(AppConstants.BUCKET_NAME, item.objectName())) {
                    String entryName = item.objectName().substring(folderPath.length());
                    zipOut.putNextEntry(new ZipEntry(entryName));

                    is.transferTo(zipOut);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    log.error(
                            "User: [{}], error occurred when transfer resource [{}] to zip",
                            MDC.get(MDC_USERNAME_KEY),
                            item.objectName());
                }
            }
            zipOut.finish();
        } catch (Exception e) {
            log.error(
                    "User: [{}], error occurred when getting folder [{}] contents recursively",
                    MDC.get(MDC_USERNAME_KEY),
                    folderPath);
        }
    }

    private void setHeaders(String objectName, HttpServletResponse response) {
        String fileNameToDownload = objectName.substring(objectName.lastIndexOf(SIGN_SLASH) + 1);
        String encodedFileName =
                URLEncoder.encode(fileNameToDownload, StandardCharsets.UTF_8).replace("+", "%20");

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFileName + "\"");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
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
        return path.endsWith(SIGN_SLASH);
    }
}
