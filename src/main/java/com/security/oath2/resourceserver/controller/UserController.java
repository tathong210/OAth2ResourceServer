package com.security.oath2.resourceserver.controller;

import com.security.oath2.resourceserver.dto.request.SignUpRequest;
import com.security.oath2.resourceserver.dto.response.ResponseData;
import com.security.oath2.resourceserver.dto.response.UserResponse;
import com.security.oath2.resourceserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("")
    ResponseData<List<UserResponse>> getAllUsers() {
        return ResponseData.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();
    }
}
