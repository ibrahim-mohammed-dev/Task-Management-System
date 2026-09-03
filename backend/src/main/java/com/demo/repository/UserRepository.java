package com.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.model.User;
public interface UserRepository extends JpaRepository<User ,Long>
{
    @EntityGraph(attributePaths = {"groups", "groups.permissions"})
    User findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Page<User> findAll(Pageable pageable);
}
