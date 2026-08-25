package dev.excsi.quickshare.dto;

import java.util.Map;
import java.util.UUID;

public record UploadUrlDto(
        UUID fileId,
        String method,
        String url,
        Map<String, String> fields,
        Map<String, String> headers
) {
}
