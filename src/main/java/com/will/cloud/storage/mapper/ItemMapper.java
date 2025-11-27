package com.will.cloud.storage.mapper;

import static com.will.cloud.storage.util.AppConstants.SIGN_SLASH;

import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.dto.response.ResourceType;

import io.minio.messages.Item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    @Mapping(
            target = "path",
            expression = "java(objectNameToPath(item.objectName(), item.isDir()))")
    @Mapping(
            target = "name",
            expression = "java(objectNameToFileName(item.objectName(), item.isDir()))")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping(target = "resourceType", expression = "java(identifyResourceType(item.objectName()))")
    MinioResourceResponseDto mapToMinioResourceResponseDto(Item item);

    @Named("objectNameToPath")
    default String objectNameToPath(String objectName, boolean isDir) {
        if (objectName.split(SIGN_SLASH).length == 2) {
            return SIGN_SLASH;
        }
        return isDir
                ? objectName.substring(
                        objectName.indexOf(SIGN_SLASH) + 1, getPreLastIndexOfSlash(objectName) + 1)
                : objectName.substring(
                        objectName.indexOf(SIGN_SLASH) + 1, objectName.lastIndexOf(SIGN_SLASH) + 1);
    }

    @Named("objectNameToFileName")
    default String objectNameToFileName(String objectName, boolean isDir) {
        if (!isDir) {
            return objectName.substring(objectName.lastIndexOf(SIGN_SLASH) + 1);
        }
        return objectName.substring(
                getPreLastIndexOfSlash(objectName) + 1, objectName.lastIndexOf(SIGN_SLASH));
    }

    @Named("identifyResourceType")
    default ResourceType identifyResourceType(String objectName) {
        return objectName.endsWith(SIGN_SLASH) ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

    private static int getPreLastIndexOfSlash(String objectName) {
        return objectName.substring(0, objectName.lastIndexOf(SIGN_SLASH)).lastIndexOf(SIGN_SLASH);
    }
}
