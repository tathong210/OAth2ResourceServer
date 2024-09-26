package com.security.oath2.resourceserver.mapper;

import com.security.oath2.resourceserver.dto.response.AuthorityResponse;
import com.security.oath2.resourceserver.entity.AuthorityType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorityMapper {
    AuthorityResponse toResponse(AuthorityType type);
}
