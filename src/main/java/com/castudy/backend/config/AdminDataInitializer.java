package com.castudy.backend.config;

import com.castudy.backend.entity.Role;
import com.castudy.backend.entity.Student;
import com.castudy.backend.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminDataInitializer {

    @Bean
    CommandLineRunner createAdmin(
            StudentRepository repository,
            BCryptPasswordEncoder encoder) {

        return args -> {

            if (repository.findByEmail("admin@castudy.com").isEmpty()) {

                Student admin = new Student();

                admin.setName("Admin");
                admin.setEmail("admin@castudy.com");
                admin.setPassword(
                        encoder.encode("admin123")
                );
                admin.setRole(Role.ROLE_ADMIN);

                repository.save(admin);

                System.out.println("Admin user created");
            }
        };
    }
}