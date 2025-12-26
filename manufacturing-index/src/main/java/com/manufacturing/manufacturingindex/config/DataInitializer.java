package com.manufacturing.manufacturingindex.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.manufacturing.manufacturingindex.model.User;
import com.manufacturing.manufacturingindex.repository.UserRepository;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initUsers() {
        return args -> {

            // cria usuário admin somente se não existir
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123"));

                userRepository.save(admin);

                System.out.println(">>> Usuário admin criado com sucesso");
            }
            else {
                System.out.println(">>> Usuário admin já existe");
            }
        };
    }
}
