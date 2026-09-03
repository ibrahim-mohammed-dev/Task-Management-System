package com.demo.repository;

import com.demo.dto.PermissionRequestDto;
import com.demo.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission ,Long>
{
    boolean existsByName(String name);
}
