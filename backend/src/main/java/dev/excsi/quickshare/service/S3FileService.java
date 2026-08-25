package dev.excsi.quickshare.service;

import dev.excsi.quickshare.dto.UploadUrlDto;
import dev.excsi.quickshare.model.FileMetadataEntity;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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

        if (!metadata.isUploaded()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (metadata.getExpiration() != null && metadata.getExpiration().isBefore(Instant.now(Clock.systemUTC()))) {
            throw new ResponseStatusException(HttpStatus.GONE);
        }
        if (metadata.getDownloadLimit() != null && metadata.getDownloadCount() >= metadata.getDownloadLimit()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (metadata.hasPassword() && (password.isEmpty() || !passwordEncoder.matches(password.get(), metadata.getPassword()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        metadata.incrementDownloadCount();

        return s3Template.createSignedGetURL(
                S3BucketName,
                objectKey(metadata),
                Duration.ofMinutes(10));
    }

    public UploadUrlDto createUploadUrl(
            UUID userId,
            String fileName,
            long fileSizeBytes,
            String contentType,
            Optional<String> password,
            Optional<Instant> expiration,
            Optional<Integer> downloadLimit
    ) {
        String uploadContentType = contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
        Optional<String> passwordHash = password
                .filter(value -> !value.isBlank())
                .map(passwordEncoder::encode);

        FileMetadataEntity metadata = fileMetadataService.createPendingUpload(
                userId,
                fileName,
                fileSizeBytes,
                passwordHash,
                expiration,
                downloadLimit
        );

        ObjectMetadata objectMetadata = ObjectMetadata.builder()
                .contentLength(fileSizeBytes)
                .contentType(uploadContentType)
                .build();

        URL uploadUrl = s3Template.createSignedPutURL(
                S3BucketName,
                objectKey(metadata),
                Duration.ofMinutes(10),
                objectMetadata,
                uploadContentType);

        return new UploadUrlDto(
                metadata.getId(),
                "PUT",
                uploadUrl.toString(),
                Map.of(),
                Map.of("Content-Type", uploadContentType));
    }

    @Transactional
    public void completeUpload(UUID fileId, UUID userId) {
        FileMetadataEntity metadata = fileMetadataService.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!metadata.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!s3Template.objectExists(S3BucketName, objectKey(metadata))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        fileMetadataService.markUploadCompleted(fileId, userId);
    }

    private String objectKey(FileMetadataEntity metadata) {
        return String.format("files/%s/%s", metadata.getOwner().getId(), metadata.getId());
    }
}
