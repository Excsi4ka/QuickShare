package dev.excsi.quickshare.dto;

import java.util.UUID;

public record UserDto(UUID uuid, String email, String name) {
}
