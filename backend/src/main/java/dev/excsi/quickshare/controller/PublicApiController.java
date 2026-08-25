package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.DownloadLinkDto;
import dev.excsi.quickshare.dto.FileInfoDto;
import dev.excsi.quickshare.model.FileMetadataEntity;
import dev.excsi.quickshare.service.FileMetadataService;
import dev.excsi.quickshare.service.S3FileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/")
public class PublicApiController {

    private final S3FileService fileService;

    private final FileMetadataService fileMetadataService;

    public PublicApiController(S3FileService fileService, FileMetadataService fileMetadataService) {
        this.fileService = fileService;
        this.fileMetadataService = fileMetadataService;
    }

    @GetMapping("/download/{fileUUID}")
    public DownloadLinkDto getDownloadLink(
            @PathVariable UUID fileUUID,
            @RequestParam(name = "password", required = false, defaultValue = "") String password
    ) {
        URL downloadLink = fileService.getPreSignedDownloadLink(fileUUID, password.describeConstable());
        return new DownloadLinkDto(downloadLink.toString());
    }

    @GetMapping("/info/{fileUUID}")
    public FileInfoDto getInfoAboutFile(
            @PathVariable UUID fileUUID
    ) {
        Optional<FileMetadataEntity> holder = fileMetadataService.findById(fileUUID);

        if (holder.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return null;
    }
}
