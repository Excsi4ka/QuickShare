package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.CreateUploadUrlDto;
import dev.excsi.quickshare.dto.UploadUrlDto;
import dev.excsi.quickshare.service.S3FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/private/")
public class PrivateApiController {

    private final S3FileService s3FileService;

    public PrivateApiController(S3FileService s3FileService) {
        this.s3FileService = s3FileService;
    }

    @PostMapping("/upload")
    public UploadUrlDto createUploadUrl(
            @RequestBody CreateUploadUrlDto uploadUrlDto,
            JwtAuthenticationToken authenticationToken
    ) {
        UUID userId = UUID.fromString(authenticationToken.getName());

        return s3FileService.createUploadUrl(
                userId,
                uploadUrlDto.fileName(),
                uploadUrlDto.fileSizeBytes(),
                uploadUrlDto.contentType(),
                Optional.ofNullable(uploadUrlDto.password()),
                Optional.ofNullable(uploadUrlDto.expiration()),
                Optional.ofNullable(uploadUrlDto.downloadLimit()));
    }

    @PostMapping("/upload/{fileUUID}/complete")
    public ResponseEntity<Void> completeUpload(
            @PathVariable UUID fileUUID,
            JwtAuthenticationToken authenticationToken
    ) {
        UUID userId = UUID.fromString(authenticationToken.getName());
        s3FileService.completeUpload(fileUUID, userId);

        return ResponseEntity.noContent().build();
    }
}
