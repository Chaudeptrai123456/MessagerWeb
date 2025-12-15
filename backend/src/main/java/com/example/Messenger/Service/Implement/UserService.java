package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.User;
import com.example.Messenger.Record.UserProfile;
import com.example.Messenger.Repository.AuthorityRepository;
import com.example.Messenger.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    public User handleLogin(String email, String username, String avatar) {
        return userRepository.findUserByEmail(email)
                .orElseGet(() -> {
                    Authority roleUser = authorityRepository.findByName("ROLE_USER")
                            .orElseGet(() -> {
                                Authority newRole = new Authority();
                                newRole.setId(UUID.randomUUID().toString());
                                newRole.setName("ROLE_USER");
                                return authorityRepository.save(newRole);
                            });
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setRegistrationDate(LocalDateTime.now());
                    newUser.setAuthorities(List.of(roleUser));
                    newUser.setAvatar(avatar);
                    return userRepository.save(newUser);
                });
    }
    public UserProfile getProfile(String token) {
        UserProfile userProfile = new UserProfile();
        return userProfile;
    }
}

