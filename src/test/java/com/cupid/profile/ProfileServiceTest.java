package com.cupid.profile;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
 * Unit tests for Allaine's Profile lifecycle component.
 *
 * Supports FR_Profile, FR_Profile_Fetch, FR_Profile_Keep_Ethics,
 * NFR_Input_Sanitise, and NFR_Traceability.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    private ProfileSettingsProperties profileSettings;
    private ProfileService profileService;
    private User alex;

    @BeforeEach
    void setUp() {
        profileSettings = new ProfileSettingsProperties();
        profileService = new ProfileService(userRepository, profileSettings);
        alex = new User(
                1L,
                "Alex",
                24,
                "Enjoys hiking.",
                true,
                OffsetDateTime.now()
        );
    }

    /**
     * FR_Profile and NFR_Input_Sanitise: a profile is stored as active after
     * markup and surrounding whitespace have been removed.
     */
    @Test
    void createProfile_shouldSanitiseAndPersistActiveProfile() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User created = profileService.createProfile(
                new ProfileForm(
                        "  <b>Alex</b>  ",
                        24,
                        " Loves <em>hiking</em>. "
                )
        );

        assertEquals("Alex", created.getDisplayName());
        assertEquals(24, created.getAge());
        assertEquals("Loves hiking.", created.getBio());
        assertTrue(created.isActive());
        verify(userRepository).save(created);
    }

    /**
     * FR_Profile: an active profile can update its displayed details.
     */
    @Test
    void updateProfile_shouldPersistChangedDetails() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));
        when(userRepository.save(alex)).thenReturn(alex);

        User updated = profileService.updateProfile(
                alex.getId(),
                new ProfileForm("Alexandra", 25, "Hiking and coffee.")
        );

        assertEquals("Alexandra", updated.getDisplayName());
        assertEquals(25, updated.getAge());
        assertEquals("Hiking and coffee.", updated.getBio());
        verify(userRepository).save(alex);
    }

    /**
     * FR_Profile_Fetch: deactivated profiles cannot be displayed.
     */
    @Test
    void findActiveProfile_shouldHideDeactivatedProfile() {
        alex.deactivate();
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        assertTrue(profileService.findActiveProfile(alex.getId()).isEmpty());
    }

    /**
     * FR_Profile_Keep_Ethics: the central setting blocks deletion by default.
     */
    @Test
    void deactivateProfile_shouldRejectWhenEthicalSettingIsDisabled() {
        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), true)
        );

        assertTrue(exception.getMessage().contains("disabled"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * FR_Profile_Keep_Ethics: after the shared setting is enabled, explicit
     * confirmation performs a soft account deletion.
     */
    @Test
    void deactivateProfile_shouldDeactivateConfirmedProfileWhenEnabled() {
        profileSettings.setAccountDeletionEnabled(true);
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        profileService.deactivateProfile(alex.getId(), true);

        assertFalse(alex.isActive());
        verify(userRepository).save(alex);
    }

    /**
     * FR_Profile_Keep_Ethics: even when the setting is enabled, the user must
     * explicitly confirm the action.
     */
    @Test
    void deactivateProfile_shouldRequireConfirmation() {
        profileSettings.setAccountDeletionEnabled(true);

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), false)
        );

        assertTrue(exception.getMessage().contains("Confirm"));
        verify(userRepository, never()).findById(alex.getId());
    }
}
