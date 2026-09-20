package com.demo.repository;

import com.demo.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission ,Long>
{
    boolean existsByName(String name);
    Optional<Permission> findByName(String name);
}
