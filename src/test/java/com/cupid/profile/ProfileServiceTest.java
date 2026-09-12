package com.cupid.profile;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * FR_Swipe_More_Ethics, FR_Message_More_Ethics,
 * NFR_Input_Sanitise, and NFR_Traceability.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    private ProfileSettingsProperties profileSettings;
    private MatchingProperties matchingSettings;
    private ProfileService profileService;
    private User alex;

    @BeforeEach
    void setUp() {
        profileSettings = new ProfileSettingsProperties();
        profileSettings.setAccountDeletionEnabled(true);
        matchingSettings = new MatchingProperties();
        profileService = new ProfileService(
                userRepository,
                profileSettings,
                matchingSettings
        );
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
     * FR_Profile_Keep_Ethics: the single global setting can disable deletion
     * for every user.
     */
    @Test
    void deactivateProfile_shouldRejectWhenGlobalEthicalSettingIsDisabled() {
        profileSettings.setAccountDeletionEnabled(false);

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), true, "DELETE")
        );

        assertTrue(exception.getMessage().contains("ethical settings"));
        verify(userRepository, never()).findById(alex.getId());
    }

    /**
     * FR_Profile_Keep_Ethics: deletion stays unavailable until the profile
     * owner deliberately enables it from Account Settings.
     */
    @Test
    void deactivateProfile_shouldRejectWhenAccountSettingsAreDisabled() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), true, "DELETE")
        );

        assertTrue(exception.getMessage().contains("Account Settings"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * FR_Profile_Keep_Ethics: saving the setting records a deliberate opt-in
     * without deactivating the profile.
     */
    @Test
    void updateAccountDeactivationPreference_shouldPersistProfileChoice() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));
        when(userRepository.save(alex)).thenReturn(alex);

        profileService.updateAccountDeactivationPreference(alex.getId(), true);

        assertTrue(alex.isAccountDeactivationEnabled());
        assertTrue(alex.isActive());
        verify(userRepository).save(alex);
    }

    /**
     * FR_Profile_Keep_Ethics: after the profile owner enables the setting,
     * acknowledgement and the exact phrase perform a soft account deletion.
     */
    @Test
    void deactivateProfile_shouldDeactivateConfirmedProfileWhenEnabled() {
        alex.updateAccountDeactivationPreference(true);
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        profileService.deactivateProfile(alex.getId(), true, "DELETE");

        assertFalse(alex.isActive());
        verify(userRepository).save(alex);
    }

    /**
     * FR_Profile_Keep_Ethics: a final confirmation is still required after
     * the profile owner enabled the setting.
     */
    @Test
    void deactivateProfile_shouldRequireConfirmation() {
        alex.updateAccountDeactivationPreference(true);
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), false, "DELETE")
        );

        assertTrue(exception.getMessage().contains("Confirm"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * FR_Profile_Keep_Ethics: a checked box is not sufficient; the profile
     * owner must type the exact deliberate confirmation word.
     */
    @Test
    void deactivateProfile_shouldRequireExactDeletionPhrase() {
        alex.updateAccountDeactivationPreference(true);
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.deactivateProfile(alex.getId(), true, "delete")
        );

        assertTrue(exception.getMessage().contains("DELETE"));
        assertTrue(alex.isActive());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * FR_Swipe_More_Ethics: recording the profile owner's opt-out for
     * coercive swipe reminders persists the choice.
     */
    @Test
    void updateSwipeEncouragementPreference_shouldPersistProfileChoice() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));
        when(userRepository.save(alex)).thenReturn(alex);

        profileService.updateSwipeEncouragementPreference(alex.getId(), false);

        assertFalse(alex.isSwipeEncouragementEnabled());
        verify(userRepository).save(alex);
    }

    /**
     * FR_Message_More_Ethics: recording the profile owner's opt-out for
     * coercive messaging pressure persists the choice.
     */
    @Test
    void updateMessageCoercionPreference_shouldPersistProfileChoice() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));
        when(userRepository.save(alex)).thenReturn(alex);

        profileService.updateMessageCoercionPreference(alex.getId(), false);

        assertFalse(alex.isMessageCoercionEnabled());
        verify(userRepository).save(alex);
    }

    /**
     * Discovery preferences: valid values are saved on the profile.
     */
    @Test
    void updateDiscoveryPreferences_shouldPersistValidChoices() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));
        when(userRepository.save(alex)).thenReturn(alex);

        profileService.updateDiscoveryPreferences(
                alex.getId(),
                false,
                "Spanish",
                120,
                21,
                35
        );

        assertFalse(alex.isShowMeOnCupid());
        assertEquals("Spanish", alex.getPreferredLanguage());
        assertEquals(120, alex.getMaxDistanceKm());
        assertEquals(21, alex.getPreferredMinAge());
        assertEquals(35, alex.getPreferredMaxAge());
        verify(userRepository).save(alex);
    }

    /**
     * Discovery preferences: minimum age above maximum is rejected.
     */
    @Test
    void updateDiscoveryPreferences_shouldRejectInvalidAgeRange() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.updateDiscoveryPreferences(
                        alex.getId(),
                        true,
                        "English",
                        80,
                        60,
                        30
                )
        );

        assertTrue(exception.getMessage().contains("minimum"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Discovery preferences: an unsupported language is rejected.
     */
    @Test
    void updateDiscoveryPreferences_shouldRejectUnsupportedLanguage() {
        when(userRepository.findById(alex.getId()))
                .thenReturn(Optional.of(alex));

        ProfileException exception = assertThrows(
                ProfileException.class,
                () -> profileService.updateDiscoveryPreferences(
                        alex.getId(),
                        true,
                        "Klingon",
                        80,
                        18,
                        60
                )
        );

        assertTrue(exception.getMessage().toLowerCase().contains("language"));
        verify(userRepository, never()).save(any(User.class));
    }
}