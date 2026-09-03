package com.demo.mapper;

import com.demo.dto.GroupRequestDto;
import com.demo.dto.GroupResponseDto;
import com.demo.model.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toEntity(GroupRequestDto groupRequestDto);
    GroupResponseDto toResponseDto(Group group);
}
