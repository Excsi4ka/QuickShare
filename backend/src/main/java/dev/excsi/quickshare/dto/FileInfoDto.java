package dev.excsi.quickshare.dto;

public record FileInfoDto(String fileName, Long fileSizeBytes, boolean needsPassword) {
}
