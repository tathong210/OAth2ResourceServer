package com.security.oath2.resourceserver.repository;

import com.security.oath2.resourceserver.entity.AuthorityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityTypeRepository extends JpaRepository<AuthorityType, Integer> {
    Optional<AuthorityType> findByName(String role);
}
