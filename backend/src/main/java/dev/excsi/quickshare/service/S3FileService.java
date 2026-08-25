package dev.excsi.quickshare.service;

import dev.excsi.quickshare.model.FileMetadataEntity;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3FileService {

    @Value("${app.s3.bucket}")
    private String S3BucketName;

    private final S3Template s3Template;

    private final FileMetadataService fileMetadataService;

    private final PasswordEncoder passwordEncoder;

    public S3FileService(
            S3Template s3Template,
            FileMetadataService fileMetadataService, PasswordEncoder passwordEncoder
    ) {
        this.s3Template = s3Template;
        this.fileMetadataService = fileMetadataService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public URL getPreSignedDownloadLink(UUID fileId, Optional<String> password) {
        Optional<FileMetadataEntity> holder = fileMetadataService.findById(fileId);

        if (holder.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        FileMetadataEntity metadata = holder.get();

        if (metadata.hasPassword() && (password.isEmpty() || !passwordEncoder.matches(password.get(), metadata.getPassword()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return s3Template.createSignedGetURL(
                S3BucketName,
                String.format("files/%s/%s", metadata.getOwner().getId(), fileId),
                Duration.ofMinutes(10));
    }

    public URL createUploadUrl(
            UUID userId,
            Optional<String> password,
            Optional<Integer> downloadLimit
    ) {
        return null;
    }
}
