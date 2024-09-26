package com.security.oath2.resourceserver.controller;

import com.security.oath2.resourceserver.dto.request.LoginRequest;
import com.security.oath2.resourceserver.dto.request.SignUpRequest;
import com.security.oath2.resourceserver.dto.response.AuthenticationResponse;
import com.security.oath2.resourceserver.dto.response.ResponseData;
import com.security.oath2.resourceserver.dto.response.UserResponse;
import com.security.oath2.resourceserver.service.AuthenticationService;
import com.security.oath2.resourceserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/sign-up")
    ResponseData<UserResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseData.<UserResponse>builder()
                .result(userService.signUp(request))
                .build();
    }

    @PostMapping("/login")
    ResponseData<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseData.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

}
