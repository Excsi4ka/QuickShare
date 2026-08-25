package dev.excsi.quickshare.dto;

import java.time.Instant;

public record CreateUploadUrlDto(
        String fileName,
        long fileSizeBytes,
        String contentType,
        String password,
        Instant expiration,
        Integer downloadLimit
) {
}
