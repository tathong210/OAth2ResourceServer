package com.security.oath2.resourceserver.dto.response;

import lombok.Getter;
import lombok.Setter;


import java.util.Set;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private Set<AuthorityResponse> roles;
}
