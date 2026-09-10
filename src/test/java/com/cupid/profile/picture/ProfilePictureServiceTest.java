package com.cupid.profile.picture;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import com.cupid.profile.picture.config.ProfilePictureProperties;
import com.cupid.profile.picture.model.ProfilePicture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Tanzim's Profile Picture upload component.
 *
 * Supports FR_Profile_Picture, NFR_Input_Sanitise, NFR_Traceability.
 */
@ExtendWith(MockitoExtension.class)
class ProfilePictureServiceTest {

    private static final byte[] REAL_JPEG_HEADER = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] REAL_PNG_HEADER =
            { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

    @Mock
    private ProfilePictureRepository pictureRepository;

    @Mock
    private UserRepository userRepository;

    private ProfilePictureProperties pictureProperties;
    private ProfilePictureService pictureService;
    private User alex;

    @BeforeEach
    void configureCollaborators() {
        pictureProperties = new ProfilePictureProperties();
        pictureService = new ProfilePictureService(
                pictureRepository,
                userRepository,
                pictureProperties
        );

        alex = new User(
                1L,
                "Alex",
                27,
                "Coffee and long walks.",
                true,
                OffsetDateTime.now()
        );
    }

    @Test
    void firstUploadBecomesPrimary() {
        MultipartFile file = jpeg("selfie.jpg", 4_000);
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));
        when(pictureRepository.countByUserId(alex.getId())).thenReturn(0L);
        when(pictureRepository.save(any(ProfilePicture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfilePicture saved = pictureService.uploadPicture(alex.getId(), file);

        assertTrue(saved.isPrimaryPicture());
        assertEquals("image/jpeg", saved.getContentType());
    }

    @Test
    void subsequentUploadIsSecondary() {
        MultipartFile file = jpeg("second.jpg", 4_000);
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));
        when(pictureRepository.countByUserId(alex.getId())).thenReturn(1L);
        when(pictureRepository.save(any(ProfilePicture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfilePicture saved = pictureService.uploadPicture(alex.getId(), file);

        assertFalse(saved.isPrimaryPicture());
    }

    @Test
    void rejectsWrongContentType() {
        MultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                new byte[] { 0x25, 0x50, 0x44, 0x46 }
        );
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));

        assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(alex.getId(), file)
        );
        verify(pictureRepository, never()).save(any());
    }

    @Test
    void rejectsMagicByteMismatch() {
        // A file that claims JPEG but has PNG magic bytes: the security check.
        MultipartFile file = new MockMultipartFile(
                "file", "sneaky.jpg", "image/jpeg", REAL_PNG_HEADER
        );
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));

        ProfilePictureException exception = assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(alex.getId(), file)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("valid image"));
        verify(pictureRepository, never()).save(any());
    }

    @Test
    void rejectsEmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));

        assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(alex.getId(), file)
        );
    }

    @Test
    void rejectsOversizedFile() {
        long capBytes = pictureProperties.getMaxFileSizeBytes();
        MultipartFile file = jpeg("huge.jpg", (int) capBytes + 1);
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));

        assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(alex.getId(), file)
        );
    }

    @Test
    void rejectsUploadForInactiveProfile() {
        User deactivated = new User(
                2L, "Blake", 30, "See you.", false, OffsetDateTime.now()
        );
        MultipartFile file = jpeg("hi.jpg", 4_000);
        when(userRepository.findById(deactivated.getId()))
                .thenReturn(Optional.of(deactivated));

        assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(deactivated.getId(), file)
        );
    }

    @Test
    void rejectsWhenGalleryIsFull() {
        MultipartFile file = jpeg("extra.jpg", 4_000);
        when(userRepository.findById(alex.getId())).thenReturn(Optional.of(alex));
        when(pictureRepository.countByUserId(alex.getId()))
                .thenReturn((long) pictureProperties.getMaxPicturesPerProfile());

        assertThrows(
                ProfilePictureException.class,
                () -> pictureService.uploadPicture(alex.getId(), file)
        );
    }

    @Test
    void promotingSecondaryDemotesExistingPrimary() {
        ProfilePicture currentPrimary = new ProfilePicture(
                alex.getId(), "image/jpeg", 100, true, REAL_JPEG_HEADER
        );
        ProfilePicture candidate = new ProfilePicture(
                alex.getId(), "image/jpeg", 100, false, REAL_JPEG_HEADER
        );
        when(pictureRepository.findById(9L)).thenReturn(Optional.of(candidate));
        when(pictureRepository.findByUserIdAndPrimaryPictureTrue(alex.getId()))
                .thenReturn(Optional.of(currentPrimary));

        pictureService.setPrimary(alex.getId(), 9L);

        ArgumentCaptor<ProfilePicture> savedCaptor =
                ArgumentCaptor.forClass(ProfilePicture.class);
        verify(pictureRepository, org.mockito.Mockito.times(2)).save(savedCaptor.capture());
        List<ProfilePicture> saves = savedCaptor.getAllValues();
        assertFalse(saves.get(0).isPrimaryPicture());
        assertTrue(saves.get(1).isPrimaryPicture());
    }

    private static MultipartFile jpeg(String filename, int totalBytes) {
        byte[] payload = new byte[totalBytes];
        System.arraycopy(
                REAL_JPEG_HEADER, 0, payload, 0, REAL_JPEG_HEADER.length
        );
        return new MockMultipartFile("file", filename, "image/jpeg", payload);
    }
}
