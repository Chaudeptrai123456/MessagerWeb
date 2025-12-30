package com.example.Messenger.Controller;

import com.example.Messenger.Service.Implement.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class TestController {
    private final GoogleDriveService googleDriveService;
    @Autowired
    public TestController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }
    @PostMapping("/admin/ads/video")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file) throws IOException {
        String url = googleDriveService.uploadVideo(file);
        return ResponseEntity.ok(url);
    }
}
