package com.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id" ,referencedColumnName = "id")
    private User user;
    @Column(nullable = false, unique = true)
    private String hashedToken;
    @Column(nullable = false)
    private Instant expiryDate;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant cretedAt;

    public RefreshToken(User user, String hashedToken, Instant expiryDate) {
        this.user = user;
        this.hashedToken = hashedToken;
        this.expiryDate = expiryDate;
    }
    //helper method for creating refresh token
    public static RefreshToken create(User user, String hashedToken, long durationMs) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setHashedToken(hashedToken);
        token.setExpiryDate(Instant.now().plusMillis(durationMs));
        return token;
    }
}
