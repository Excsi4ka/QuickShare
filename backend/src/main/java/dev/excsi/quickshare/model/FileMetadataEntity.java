package dev.excsi.quickshare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metas")
public class FileMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "has_password", nullable = false)
    private boolean hasPassword;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "size", nullable = false)
    private long byteSize;

    @Column(name = "expire_at", nullable = true)
    private Instant expiration;

    @Column(name = "download_limit", nullable = true)
    private Integer downloadLimit;

    @Column(name = "download_count", nullable = false)
    private int downloadCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploading_status", nullable = false)
    private UploadingStatus uploadingStatus;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    public FileMetadataEntity() {
    }

    public FileMetadataEntity(
            UserEntity user,
            String name,
            boolean hasPassword,
            String password,
            long byteSize,
            Instant expiration,
            Integer downloadLimit
    ) {
        this.user = user;
        this.name = name;
        this.hasPassword = hasPassword;
        this.password = password;
        this.byteSize = byteSize;
        this.expiration = expiration;
        this.downloadLimit = downloadLimit;
        this.downloadCount = 0;
        this.uploadingStatus = UploadingStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getOwner() {
        return user;
    }

    public String getName() {
        return name;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public String getPassword() {
        return password;
    }

    public long getByteSize() {
        return byteSize;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public Integer getDownloadLimit() {
        return downloadLimit;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public UploadingStatus getUploadingStatus() {
        return uploadingStatus;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public boolean isUploaded() {
        return uploadingStatus == UploadingStatus.UPLOADED;
    }

    public void markUploaded() {
        this.uploadingStatus = UploadingStatus.UPLOADED;
        this.uploadedAt = Instant.now(Clock.systemUTC());
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public enum UploadingStatus {
        PENDING,
        UPLOADED
    }
}
