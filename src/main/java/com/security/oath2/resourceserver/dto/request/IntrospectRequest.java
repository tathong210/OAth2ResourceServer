package com.security.oath2.resourceserver.dto.request;

import lombok.Builder;

@Builder
public record IntrospectRequest (String token){
}
