package com.example.Messenger.Service.Implement;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class GoogleDriveService {

    private final Drive drive;

    private static final String ADS_FOLDER_ID = "ID_FOLDER_ADS";
    @Autowired
    public GoogleDriveService(Drive drive) {
        this.drive = drive;
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        File metadata = new File();
        metadata.setName(file.getOriginalFilename());
        metadata.setParents(List.of(ADS_FOLDER_ID));
        InputStreamContent content = new InputStreamContent(
                file.getContentType(),
                file.getInputStream()
        );
        File uploaded = drive.files()
                .create(metadata, content)
                .setFields("id, webViewLink, webContentLink")
                .execute();
        return uploaded.getWebViewLink();
    }
}
