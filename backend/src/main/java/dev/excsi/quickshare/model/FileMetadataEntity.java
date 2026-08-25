package dev.excsi.quickshare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @Column(name = "has_password", nullable = false)
    private boolean hasPassword;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "size", nullable = false)
    private long byteSize;

    @Column(name = "expire_at", nullable = true)
    private Instant expiration;

    @Column(name = "download_limit", nullable = true)
    private int downloadLimit;

    public FileMetadataEntity() {
    }

    public UserEntity getOwner() {
        return user;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public String getPassword() {
        return password;
    }
}
