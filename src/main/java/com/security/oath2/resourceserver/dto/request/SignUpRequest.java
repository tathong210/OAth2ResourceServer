package com.security.oath2.resourceserver.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {
    @NotNull(message = "Required Username")
    private String username;
    @NotNull(message = "Required password")
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;

    private List<String> roles;
}
