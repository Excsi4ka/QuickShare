package dev.excsi.quickshare.service;

import dev.excsi.quickshare.model.FileMetadataEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FileMetadataService {

    public Optional<FileMetadataEntity> findById(UUID fileMetadataId) {
        return Optional.empty();
    }
}
