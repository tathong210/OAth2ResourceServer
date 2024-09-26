package com.security.oath2.resourceserver.mapper;

import com.security.oath2.resourceserver.dto.request.SignUpRequest;
import com.security.oath2.resourceserver.dto.response.UserResponse;
import com.security.oath2.resourceserver.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AuthorityMapper.class})
public interface UserMapper {
    @Mapping(source = "authorities", target = "roles")
    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toUser(SignUpRequest request);
}
