package com.demo.config;

import com.demo.model.Group;
import com.demo.model.Permission;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.PermissionRepository;
import com.demo.repository.UserRepository;
import com.demo.security.AppPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final GroupRepository      groupRepository;
    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.users.default-permissions}")
    private List<String> usersDefaultPermissions;

    @Override
    @Transactional
    public void run(String... args) {
        log.info(">>> DataInitializer: starting data seeding...");

        seedPermissions();

        seedGroup("USERS",       usersDefaultPermissions);
        Group superAdminGroup = seedGroup("SUPER_ADMIN", List.of());

        seedAdminUser(superAdminGroup);

        log.info(">>> DataInitializer: seeding complete.");
    }

    private void seedPermissions() {
        for (AppPermission permission : AppPermission.values()) {
            if (!permissionRepository.existsByName(permission.name())) {
                Permission entity = new Permission();
                entity.setName(permission.name());
                permissionRepository.save(entity);
                log.info("  Created permission: {}", permission.name());
            }
        }
    }

    private Group seedGroup(String groupName, List<String> permissionNames) {
        Group group = groupRepository.findByName(groupName).orElseGet(() -> {
            Group g = new Group();
            g.setName(groupName);
            Group saved = groupRepository.save(g);
            log.info("  Created group: {}", groupName);
            return saved;
        });

        for (String permName : permissionNames) {
            permissionRepository.findByName(permName).ifPresent(permission -> {
                if (!group.getPermissions().contains(permission)) {
                    group.getPermissions().add(permission);
                    log.info("  Assigned '{}' → group '{}'", permName, groupName);
                }
            });
        }

        return groupRepository.save(group);
    }

    private void seedAdminUser(Group superAdminGroup) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("  Admin '{}' already exists — skipping.", adminEmail);
            return;
        }

        User admin = new User(adminUsername, adminEmail,
                passwordEncoder.encode(adminPassword));

        admin = userRepository.save(admin);
        superAdminGroup.getUsers().add(admin);
        groupRepository.save(superAdminGroup);

        log.info("  Created admin '{}' assigned to SUPER_ADMIN.", adminUsername);
    }
}