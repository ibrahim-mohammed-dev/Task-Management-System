package com.demo.repository;

import com.demo.model.RefreshToken;
import com.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>
{
    Optional<RefreshToken> findByHashedToken(String hashedToken);
    int deleteByUser(User user);
    int deleteByUserUsername(String username);
}
