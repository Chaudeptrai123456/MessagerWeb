package com.example.Messenger.Record;
public record ImageRequest(
        String filename,
        String contentType,
        String base64Data // ảnh base64 từ frontend
) {}
