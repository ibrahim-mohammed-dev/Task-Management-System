package com.demo.mapper;

import com.demo.dto.PermissionRequestDto;
import com.demo.dto.PermissionResponseDto;
import com.demo.model.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponseDto toResponseDto(Permission permission);
    Permission toEntity(PermissionRequestDto permissionRequestDto);
}
