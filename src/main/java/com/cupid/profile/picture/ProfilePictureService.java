package com.cupid.profile.picture;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import com.cupid.profile.picture.config.ProfilePictureProperties;
import com.cupid.profile.picture.model.ProfilePicture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Picture-upload business rules for a Cupid profile.
 *
 * Supports: FR_Profile_Picture, NFR_Input_Sanitise, NFR_Persistence_Timeout,
 * NFR_Traceability.
 *
 * Bytes are validated three ways before persistence:
 *   1. Spring's multipart limit stops the request at the boundary.
 *   2. The declared Content-Type must be on the configuration whitelist.
 *   3. Magic bytes must match the declared type. This detects an executable
 *      renamed as .jpg and is the security-critical check.
 */
@Service
public class ProfilePictureService {

    private static final byte[] JPEG_MAGIC = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    // File signatures support content validation independently of a file name.
    private static final byte[] PNG_MAGIC =
            { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] WEBP_RIFF = { 0x52, 0x49, 0x46, 0x46 };
    private static final byte[] WEBP_TAG = { 0x57, 0x45, 0x42, 0x50 };

    private final ProfilePictureRepository pictureRepository;
    private final UserRepository userRepository;
    private final ProfilePictureProperties pictureProperties;

    public ProfilePictureService(
            ProfilePictureRepository pictureRepository,
            UserRepository userRepository,
            ProfilePictureProperties pictureProperties
    ) {
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
        this.pictureProperties = pictureProperties;
    }

    /**
     * FR_Profile_Picture: accepts a new picture for an active Cupid profile.
     * The first picture uploaded automatically becomes the primary picture.
     */
    @Transactional
    public ProfilePicture uploadPicture(Long profileId, MultipartFile file) {
        User owner = requireActiveProfile(profileId);
        byte[] bytes = readAndValidate(file);
        String contentType = normaliseContentType(file.getContentType());

        long existingCount = pictureRepository.countByUserId(owner.getId());
        if (existingCount >= pictureProperties.getMaxPicturesPerProfile()) {
            throw new ProfilePictureException(
                    "Profiles can hold at most "
                            + pictureProperties.getMaxPicturesPerProfile()
                            + " pictures."
            );
        }

        boolean shouldBePrimary = existingCount == 0L;
        ProfilePicture picture = new ProfilePicture(
                owner.getId(),
                contentType,
                bytes.length,
                shouldBePrimary,
                bytes
        );

        return pictureRepository.save(picture);
    }

    /**
     * FR_Profile_Picture: lists a profile's pictures newest-first so the
     * gallery mirrors upload order.
     */
    @Transactional(readOnly = true)
    public List<ProfilePicture> listPictures(Long profileId) {
        requireActiveProfile(profileId);
        return pictureRepository.findByUserIdOrderByUploadedAtDesc(profileId);
    }

    /**
     * FR_Profile_Picture: fetches picture bytes for HTTP serving.
     */
    @Transactional(readOnly = true)
    public ProfilePicture getPicture(Long profileId, Long pictureId) {
        ProfilePicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ProfilePictureException(
                        "The requested picture could not be found."
                ));

        if (!picture.getUserId().equals(profileId)) {
            throw new ProfilePictureException(
                    "The requested picture does not belong to this profile."
            );
        }

        return picture;
    }

    /**
     * FR_Profile_Picture: promotes a picture to primary and demotes any
     * previous primary. Runs in a single transaction so only one primary
     * is visible at a time.
     */
    @Transactional
    public void setPrimary(Long profileId, Long pictureId) {
        ProfilePicture nextPrimary = getPicture(profileId, pictureId);
        if (nextPrimary.isPrimaryPicture()) {
            return;
        }

        Optional<ProfilePicture> currentPrimary =
                pictureRepository.findByUserIdAndPrimaryPictureTrue(profileId);
        currentPrimary.ifPresent(previous -> {
            previous.demoteFromPrimary();
            pictureRepository.save(previous);
        });

        nextPrimary.promoteToPrimary();
        pictureRepository.save(nextPrimary);
    }

    /**
     * FR_Profile_Picture: removes a picture. When the primary is removed
     * the newest remaining picture is promoted so the profile is not left
     * without a card image.
     */
    @Transactional
    public void deletePicture(Long profileId, Long pictureId) {
        ProfilePicture target = getPicture(profileId, pictureId);
        boolean removedPrimary = target.isPrimaryPicture();
        pictureRepository.delete(target);

        if (!removedPrimary) {
            return;
        }

        List<ProfilePicture> remaining =
                pictureRepository.findByUserIdOrderByUploadedAtDesc(profileId);
        if (remaining.isEmpty()) {
            return;
        }

        ProfilePicture successor = remaining.get(0);
        successor.promoteToPrimary();
        pictureRepository.save(successor);
    }

    private User requireActiveProfile(Long profileId) {
        if (profileId == null || profileId <= 0) {
            throw new ProfilePictureException("A valid profile is required.");
        }

        return userRepository.findById(profileId)
                .filter(User::isActive)
                .orElseThrow(() -> new ProfilePictureException(
                        "The requested active profile could not be found."
                ));
    }

    /**
     * NFR_Input_Sanitise: applies the full three-layer validation. The magic
     * bytes check is the important part: it catches files that lie about
     * their extension or Content-Type header.
     */
    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProfilePictureException("Choose a picture file to upload.");
        }

        if (file.getSize() > pictureProperties.getMaxFileSizeBytes()) {
            throw new ProfilePictureException(
                    "Pictures must be "
                            + (pictureProperties.getMaxFileSizeBytes() / (1024 * 1024))
                            + " MB or smaller."
            );
        }

        String declaredType = normaliseContentType(file.getContentType());
        if (!pictureProperties.getAllowedContentTypes().contains(declaredType)) {
            throw new ProfilePictureException(
                    "Only JPEG, PNG or WebP images can be uploaded."
            );
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ioException) {
            throw new ProfilePictureException(
                    "Unable to read the uploaded picture.",
                    ioException
            );
        }

        if (!matchesMagicBytes(bytes, declaredType)) {
            throw new ProfilePictureException(
                    "The uploaded file is not a valid image."
            );
        }

        return bytes;
    }

    private String normaliseContentType(String rawContentType) {
        if (rawContentType == null) {
            return "";
        }
        return rawContentType.trim().toLowerCase();
    }

    private boolean matchesMagicBytes(byte[] bytes, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> startsWith(bytes, JPEG_MAGIC);
            case "image/png" -> startsWith(bytes, PNG_MAGIC);
            case "image/webp" -> startsWith(bytes, WEBP_RIFF)
                    && bytes.length >= 12
                    && startsWithAtOffset(bytes, WEBP_TAG, 8);
            default -> false;
        };
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        return startsWithAtOffset(bytes, prefix, 0);
    }

    private boolean startsWithAtOffset(byte[] bytes, byte[] fragment, int offset) {
        if (bytes.length < offset + fragment.length) {
            return false;
        }
        for (int index = 0; index < fragment.length; index++) {
            if (bytes[offset + index] != fragment[index]) {
                return false;
            }
        }
        return true;
    }
}
