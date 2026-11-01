
package com.castudy.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = System.getenv("NEW_ADMIN_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Set the NEW_ADMIN_PASSWORD environment variable first."
            );
        }

        System.out.println(encoder.encode(password));
    }
}
