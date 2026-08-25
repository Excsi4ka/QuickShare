package dev.excsi.quickshare.service;

import dev.excsi.quickshare.exception.BadInputException;
import dev.excsi.quickshare.model.FileMetadataEntity;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.repository.FileMetadataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    private final UserService userService;

    public FileMetadataService(FileMetadataRepository fileMetadataRepository, UserService userService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.userService = userService;
    }

    public Optional<FileMetadataEntity> findById(UUID fileMetadataId) {
        return fileMetadataRepository.findById(fileMetadataId);
    }

    @Transactional
    public FileMetadataEntity createPendingUpload(
            UUID userId,
            String fileName,
            long byteSize,
            Optional<String> password,
            Optional<Instant> expiration,
            Optional<Integer> downloadLimit
    ) {
        if (fileName == null || fileName.isBlank())
            throw new BadInputException("File name cannot be blank");
        if (byteSize <= 0)
            throw new BadInputException("File size must be greater than 0");
        if (expiration.isPresent() && expiration.get().isBefore(Instant.now(Clock.systemUTC())))
            throw new BadInputException("Expiration cannot be in the past");
        if (downloadLimit.isPresent() && downloadLimit.get() <= 0)
            throw new BadInputException("Download limit must be greater than 0");

        UserEntity user = userService.getUserByUUID(userId);
        FileMetadataEntity metadata = new FileMetadataEntity(
                user,
                fileName,
                password.isPresent(),
                password.orElse(null),
                byteSize,
                expiration.orElse(null),
                downloadLimit.orElse(null)
        );

        return fileMetadataRepository.save(metadata);
    }

    @Transactional
    public void markUploadCompleted(UUID fileMetadataId, UUID userId) {
        FileMetadataEntity metadata = fileMetadataRepository.findById(fileMetadataId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!metadata.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        metadata.markUploaded();
    }
}
