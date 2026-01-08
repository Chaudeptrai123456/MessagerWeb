package com.example.Messenger.Record;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String username,
        String email,
        String avatar,
        LocalDateTime registrationDate
) {}
