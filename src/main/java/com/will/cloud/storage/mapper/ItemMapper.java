package com.will.cloud.storage.mapper;

import com.will.cloud.storage.dto.response.MinioResourceResponseDto;
import com.will.cloud.storage.dto.response.ResourceType;

import io.minio.messages.Item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    @Mapping(target = "path", expression = "java(objectNameToPath(item.objectName()))")
    @Mapping(target = "name", expression = "java(objectNameToFileName(item.objectName()))")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping(target = "resourceType", expression = "java(identifyResourceType(item.isDir()))")
    MinioResourceResponseDto mapToMinioResourceResponseDto(Item item);

    @Named("objectNameToPath")
    default String objectNameToPath(String objectName) {
        return objectName.substring(objectName.indexOf("/") + 1, objectName.lastIndexOf("/") + 1);
    }

    @Named("objectNameToFileName")
    default String objectNameToFileName(String objectName) {
        return objectName.substring(objectName.lastIndexOf("/") + 1);
    }

    @Named("identifyResourceType")
    default ResourceType identifyResourceType(boolean isDir) {
        return isDir ? ResourceType.DIRECTORY : ResourceType.FILE;
    }
}
