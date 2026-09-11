package com.cupid.profile.picture.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * A picture uploaded to a Cupid profile.
 *
 * Requirement: FR_Profile_Picture.
 *
 * Image bytes are stored in the database rather than on the file system so
 * that the component is portable across every teammate's machine and does
 * not depend on a shared uploads folder. Content type and size are kept
 * alongside the bytes so pictures can be served without re-reading them.
 */
@Entity
@Table(name = "profile_pictures")
public class ProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    /**
     * FR_Profile_Picture: the primary flag drives which picture appears
     * on the profile card. Only one primary is kept per user.
     */
    @Column(name = "primary_picture", nullable = false)
    private boolean primaryPicture;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "uploaded_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    protected ProfilePicture() {
        // Required by JPA; application callers use the full constructor.
    }

    public ProfilePicture(
            Long userId,
            String contentType,
            long fileSize,
            boolean primaryPicture,
            byte[] data
    ) {
        this.userId = userId;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.primaryPicture = primaryPicture;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isPrimaryPicture() {
        return primaryPicture;
    }

    public byte[] getData() {
        return data;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * FR_Profile_Picture: transfers primary-picture status. The previous
     * primary must be demoted by the service before another is promoted so
     * only one primary picture is kept per user.
     */
    public void promoteToPrimary() {
        this.primaryPicture = true;
    }

    public void demoteFromPrimary() {
        this.primaryPicture = false;
    }
}
