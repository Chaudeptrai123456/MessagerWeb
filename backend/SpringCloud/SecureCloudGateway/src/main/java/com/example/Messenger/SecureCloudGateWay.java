package com.example.Messenger;

import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.User;
import com.example.Messenger.Repository.AuthorityRepository;
import com.example.Messenger.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.example") // Quét các @Component, @Service, @Controller
@EnableJpaRepositories("com.example.Messenger.Repository")
public class SecureCloudGateWay {
    public static void main(String[] args) {
        SpringApplication.run(SecureCloudGateWay.class, args);
    }

    @Bean
    CommandLineRunner initRoles(AuthorityRepository authorityRepository, UserRepository userRepository) {
        return args -> {
//            // Kiểm tra xem đã có ROLE_USER chưa
//            boolean existsUser = authorityRepository.findByName("USER").isPresent();
//            boolean existsAdmin = authorityRepository.findByName("ADMIN").isPresent();
//            List<User> all = userRepository.findAll();
//            all.forEach(index->{
//                System.out.println("user " + index.getId()+" role " + index.getAuthorities().get(0).getName().toString());
//            });
//            if (!existsUser) {
//                Authority roleUser = new Authority();
//                roleUser.setId("user");
//                roleUser.setName("USER");
//                authorityRepository.save(roleUser);
//                System.out.println("✅ ROLE_USER created!");
//            } else {
//                System.out.println("ℹ️ ROLE_USER already exists.");
//            }
//            if (!existsAdmin) {
//                Authority roleUser = new Authority();
//                roleUser.setId("admin");
//                roleUser.setName("ADMIN");
//                authorityRepository.save(roleUser);
//                System.out.println("✅ ROLE_USER created!");
//            } else {
//                System.out.println("ℹ️ ROLE_USER already exists.");
//            }
        };
    }
}