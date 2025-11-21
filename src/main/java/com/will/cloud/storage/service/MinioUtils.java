package com.will.cloud.storage.service;

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

    private final MinioClient minioClient;

    /**
     * init Bucket when start SpringBoot container create bucket if the bucket is not exists
     *
     * @param bucketName
     */
    @SneakyThrows(Exception.class)
    public void createBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * verify Bucket is exist?，true：false
     *
     * @param bucketName
     * @return
     */
    @SneakyThrows(Exception.class)
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * check file is exist
     *
     * @param bucketName
     * @param objectName
     * @return
     */
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

    /**
     * check directory exist?
     *
     * @param bucketName
     * @param objectName
     * @return
     */
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

    /**
     * Query files based on file prefix
     *
     * @param bucketName
     * @param prefix
     * @return MinioItem
     */
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

    /**
     * get file InputStream
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows(Exception.class)
    public InputStream getObject(String bucketName, String objectName) {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * Breakpoint download
     *
     * @param bucketName
     * @param objectName
     * @param offset
     * @param length
     * @return
     */
    @SneakyThrows(Exception.class)
    public InputStream getObject(String bucketName, String objectName, long offset, long length) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .offset(offset)
                        .length(length)
                        .build());
    }

    /**
     * Get the list of files under the path
     *
     * @param bucketName
     * @param prefix
     * @param recursive
     * @return
     */
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(recursive)
                        .build());
    }

    /**
     * use MultipartFile to upload files
     *
     * @param bucketName
     * @param file
     * @param objectName
     * @param contentType
     * @return
     */
    @SneakyThrows(Exception.class)
    public ObjectWriteResponse uploadFile(
            String bucketName, MultipartFile file, String objectName, String contentType) {
        InputStream inputStream = file.getInputStream();
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .contentType(contentType)
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
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

    /**
     * upload local files
     *
     * @param bucketName
     * @param objectName
     * @param fileName
     * @return
     */
    @SneakyThrows(Exception.class)
    public ObjectWriteResponse uploadFile(String bucketName, String objectName, String fileName) {
        return minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .filename(fileName)
                        .build());
    }

    /**
     * upload files based on stream
     *
     * @param bucketName
     * @param objectName
     * @param inputStream
     * @return
     */
    @SneakyThrows(Exception.class)
    public ObjectWriteResponse uploadFile(
            String bucketName, String objectName, InputStream inputStream) {
        return minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                inputStream, inputStream.available(), -1)
                        .build());
    }

    /**
     * create file or directory
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows(Exception.class)
    public ObjectWriteResponse createDir(String bucketName, String objectName) {
        return minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
    }

    /**
     * get file info
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    @SneakyThrows(Exception.class)
    public String getFileStatusInfo(String bucketName, String objectName) {
        return minioClient
                .statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build())
                .toString();
    }

    /**
     * copy file
     *
     * @param bucketName
     * @param objectName
     * @param srcBucketName
     * @param srcObjectName
     */
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

    /**
     * delete file
     *
     * @param bucketName
     * @param objectName
     */
    @SneakyThrows(Exception.class)
    public void removeFile(String bucketName, String objectName) {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
}
