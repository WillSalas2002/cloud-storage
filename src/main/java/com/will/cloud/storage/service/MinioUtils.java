package com.will.cloud.storage.service;

import static com.will.cloud.storage.util.AppConstants.*;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private static final long SMALL_FILE_THRESHOLD = 5 * 1024 * 1024L;

    private final MinioClient minioClient;

    @SneakyThrows(Exception.class)
    public void createBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows(Exception.class)
    private boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    public boolean isObjectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("[MinioUtils]>>>>check file exist, Exception：", e);
            exist = false;
        }
        return exist;
    }

    public boolean isFolderExist(String bucketName, String objectName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results =
                    minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(bucketName)
                                    .prefix(objectName)
                                    .recursive(false)
                                    .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            log.error("[MinioUtils]>>>>check file exist, Exception：", e);
            exist = false;
        }
        return exist;
    }

    @SneakyThrows(Exception.class)
    public Item getObjectByPath(String bucketName, String prefix) {
        List<Item> list = new ArrayList<>();
        Iterable<Result<Item>> objectsIterator =
                minioClient.listObjects(
                        ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).build());
        if (objectsIterator != null) {
            for (Result<Item> o : objectsIterator) {
                Item item = o.get();
                list.add(item);
            }
        }
        return list.getFirst();
    }

    @SneakyThrows(Exception.class)
    public InputStream getObject(String bucketName, String objectName) {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(recursive)
                        .build());
    }

    @SneakyThrows(Exception.class)
    public void uploadFile(
            String bucketName, MultipartFile file, String objectName, String contentType) {
        long partSize = Math.max(SMALL_FILE_THRESHOLD, file.getSize() + 1);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(inputStream, file.getSize(), partSize)
                            .build());
        }
    }

    //    /**
    //     * picture upload
    //     * @param bucketName
    //     * @param imageBase64
    //     * @param imageName
    //     * @return
    //     */
    //    public ObjectWriteResponse uploadImage(String bucketName, String imageBase64, String
    // imageName) {
    //        if (!StringUtils.isEmpty(imageBase64)) {
    //            InputStream in = base64ToInputStream(imageBase64);
    //            String newName = System.currentTimeMillis() + "_" + imageName + ".jpg";
    //            String year = String.valueOf(new Date().getYear());
    //            String month = String.valueOf(new Date().getMonth());
    //            return uploadFile(bucketName, year + "/" + month + "/" + newName, in);
    //
    //        }
    //        return null;
    //    }
    //
    //    public static InputStream base64ToInputStream(String base64) {
    //        ByteArrayInputStream stream = null;
    //        try {
    //            byte[] bytes = new BASE64Decoder().decodeBuffer(base64.trim());
    //            stream = new ByteArrayInputStream(bytes);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return stream;
    //    }

    @SneakyThrows(Exception.class)
    public ObjectWriteResponse uploadFile(String bucketName, String objectName, String fileName) {
        return minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .filename(fileName)
                        .build());
    }

    @SneakyThrows(Exception.class)
    public ObjectWriteResponse uploadFile(
            String bucketName, String objectName, InputStream inputStream) {
        return minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                inputStream, inputStream.available(), -1)
                        .build());
    }

    @SneakyThrows(Exception.class)
    public void createDir(String bucketName, String objectName) {
        objectName = objectName.endsWith(SIGN_SLASH) ? objectName : objectName + SIGN_SLASH;
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    @SneakyThrows(Exception.class)
    public String getFileStatusInfo(String bucketName, String objectName) {
        return minioClient
                .statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build())
                .toString();
    }

    @SneakyThrows(Exception.class)
    public ObjectWriteResponse copyFile(
            String bucketName, String objectName, String srcBucketName, String srcObjectName) {
        return minioClient.copyObject(
                CopyObjectArgs.builder()
                        .source(CopySource.builder().bucket(bucketName).object(objectName).build())
                        .bucket(srcBucketName)
                        .object(srcObjectName)
                        .build());
    }

    @SneakyThrows(Exception.class)
    public void removeFile(String bucketName, String objectName) {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
}
