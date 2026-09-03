package com.demo.repository;

import com.demo.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository <Group, Long>
{
    boolean existsByIdAndUsersId(Long groupId, Long userId);
    boolean existsByIdAndPermissionsId(Long groupId, Long perId);
    boolean existsByName(String name);
    Optional<Group> findByName(String name);
}
