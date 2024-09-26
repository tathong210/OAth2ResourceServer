package com.security.oath2.resourceserver.service;

import com.security.oath2.resourceserver.dto.request.SignUpRequest;
import com.security.oath2.resourceserver.dto.response.UserResponse;
import com.security.oath2.resourceserver.entity.AuthorityType;
import com.security.oath2.resourceserver.entity.User;
import com.security.oath2.resourceserver.exception.CommonException;
import com.security.oath2.resourceserver.exception.ErrorCode;
import com.security.oath2.resourceserver.mapper.UserMapper;
import com.security.oath2.resourceserver.repository.AuthorityTypeRepository;
import com.security.oath2.resourceserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    AuthorityTypeRepository authorityTypeRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse signUp(SignUpRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<AuthorityType> authorities = new HashSet<>();
        List<String> roles = CollectionUtils.isEmpty(request.getRoles())
                ? Arrays.asList("USER")
                : request.getRoles();
        roles.forEach(role -> {
            authorityTypeRepository.findByName(role).ifPresent(authorities::add);
        });

        user.setAuthorities(authorities);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new CommonException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).collect(Collectors.toList());
    }
}
