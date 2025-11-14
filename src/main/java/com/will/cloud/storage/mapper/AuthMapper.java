package com.will.cloud.storage.mapper;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    User toUser(AuthRequest request);

    AuthResponse toAuthResponse(User user);
}
