package com.example.Messenger.Record.Response;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String username,
        String email,
        String avatar,
        LocalDateTime registrationDate
) {}
