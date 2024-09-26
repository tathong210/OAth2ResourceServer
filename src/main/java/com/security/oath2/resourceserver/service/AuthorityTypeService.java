package com.security.oath2.resourceserver.service;

import com.security.oath2.resourceserver.repository.AuthorityTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorityTypeService {
    AuthorityTypeRepository authorityTypeRepository;
}
