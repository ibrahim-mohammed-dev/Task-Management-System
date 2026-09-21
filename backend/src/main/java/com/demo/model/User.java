package com.demo.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // مطلوب إجباري من JPA
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter(AccessLevel.NONE) // مُدار يدوياً عبر UserDetails
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Getter(AccessLevel.NONE) // مُدار يدوياً عبر UserDetails
    @Column(nullable = false)
    private String password;

    @ManyToMany(mappedBy = "users" ,fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

    // Constructor واضح وصريح للـ Registration بس
    public User(String username, String email, String password ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.groups = new HashSet<>();
    }

    // ── UserDetails interface ──────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // إذا لم يكن لدى المستخدم أي مجموعات، نرجع قائمة فارغة تجنباً للـ NullPointerException
        if (this.groups == null) {
            return Collections.emptyList();
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Group group : this.groups) {
            // a) إضافة اسم المجموعة مسبوقاً بـ "GROUP_" (مثل GROUP_SUPER_ADMIN, GROUP_USERS)
            authorities.add(new SimpleGrantedAuthority("GROUP_" + group.getName()));

            // b) إضافة الصلاحيات الفردية المستخرجة من المجموعة (مثل READ_TASK, WRITE_TASK)
            group.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                    .forEach(authorities::add);
        }

        return authorities;
    }

    @Override public String getPassword()               { return password; }
    @Override public String getUsername()               { return username; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return true; }
}
