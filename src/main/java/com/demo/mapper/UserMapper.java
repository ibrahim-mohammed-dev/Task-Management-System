package com.demo.mapper;

import com.demo.dto.UserResponseDto;
import com.demo.model.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper
{
    UserResponseDto toResponseDto(User users);
}
