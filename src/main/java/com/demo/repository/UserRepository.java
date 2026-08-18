package com.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.model.User;
public interface UserRepository extends JpaRepository<User ,Long>
{
    User findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

}
