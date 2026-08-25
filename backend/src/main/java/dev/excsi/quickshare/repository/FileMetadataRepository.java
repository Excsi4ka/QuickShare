package dev.excsi.quickshare.repository;

import dev.excsi.quickshare.model.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, UUID> {
}
