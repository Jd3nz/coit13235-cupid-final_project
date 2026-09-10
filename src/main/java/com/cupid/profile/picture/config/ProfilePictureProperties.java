package com.cupid.profile.picture.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Shared configuration for profile-picture uploads.
 *
 * Requirements: FR_Profile_Picture, NFR_Input_Sanitise.
 *
 * The defaults refuse anything but JPEG, PNG and WebP images and cap
 * uploads at 5 MB per file. Values are declared in application.properties
 * under the cupid.profile.pictures prefix so the operations team can adjust
 * them without a code change.
 */
@ConfigurationProperties(prefix = "cupid.profile.pictures")
public class ProfilePictureProperties {

    /**
     * NFR_Input_Sanitise: only content types on this whitelist are stored.
     */
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    /**
     * Maximum accepted file size in bytes. Spring's multipart limit
     * defends the request boundary; this value is the second, defensive
     * check inside the service layer.
     */
    private long maxFileSizeBytes = 5L * 1024L * 1024L;

    /**
     * FR_Profile_Picture: caps the number of pictures per profile to
     * keep the gallery manageable in the interface.
     */
    private int maxPicturesPerProfile = 6;

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getMaxPicturesPerProfile() {
        return maxPicturesPerProfile;
    }

    public void setMaxPicturesPerProfile(int maxPicturesPerProfile) {
        this.maxPicturesPerProfile = maxPicturesPerProfile;
    }
}
