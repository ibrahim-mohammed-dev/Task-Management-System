package com.demo.config;

import com.demo.model.Group;
import com.demo.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;

    @Override
    public void run(String... args) {
        // التحقق مما إذا كان الجروب الافتراضي موجوداً، وإذا لم يكن موجوداً يتم إنشاؤه
        if (!groupRepository.existsByName("USERS")) {
            Group defaultGroup = new Group();
            defaultGroup.setName("USERS");
            groupRepository.save(defaultGroup);
        }
    }
}