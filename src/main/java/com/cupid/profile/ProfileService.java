package com.cupid.profile;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Profile lifecycle business rules.
 *
 * Supports: FR_Profile, FR_Profile_Fetch, FR_Profile_Keep_Ethics,
 * FR_Swipe_More_Ethics, FR_Message_More_Ethics,
 * NFR_Input_Sanitise, and NFR_Traceability.
 */
@Service
public class ProfileService {

    /**
     * Languages accepted by the discovery-preferences form. Any language outside this list is rejected before persistence.
     */
    public static final List<String> SUPPORTED_LANGUAGES = List.of(
            "English",
            "Spanish",
            "French",
            "German",
            "Portuguese",
            "Italian",
            "Japanese",
            "Korean",
            "Mandarin"
    );

    private static final int MIN_DISTANCE_KM = 1;
    private static final int MAX_DISTANCE_KM = 500;
    private static final int MIN_PREFERRED_AGE = 18;
    private static final int MAX_PREFERRED_AGE = 120;

    private final UserRepository userRepository;
    private final ProfileSettingsProperties profileSettings;
    private final MatchingProperties matchingSettings;

    public ProfileService(
            UserRepository userRepository,
            ProfileSettingsProperties profileSettings,
            MatchingProperties matchingSettings
    ) {
        this.userRepository = userRepository;
        this.profileSettings = profileSettings;
        this.matchingSettings = matchingSettings;
    }

    /**
     * FR_Profile_Fetch: only active profiles can be displayed or edited
     */
    @Transactional(readOnly = true)
    public Optional<User> findActiveProfile(Long profileId) {
        if (profileId == null || profileId <= 0) {
            return Optional.empty();
        }

        return userRepository.findById(profileId)
                .filter(User::isActive);
    }

    /**
     * FR_Profile: persists a new, active Cupid profile.
     */
    @Transactional
    public User createProfile(ProfileForm form) {
        SanitisedProfile profile = sanitiseAndValidate(form);

        User user = new User(
                null,
                profile.displayName(),
                profile.age(),
                profile.bio(),
                true,
                null
        );

        return userRepository.save(user);
    }

    /**
     * FR_Profile: updates an existing active profile.
     */
    @Transactional
    public User updateProfile(Long profileId, ProfileForm form) {
        User user = requireActiveProfile(profileId);
        SanitisedProfile profile = sanitiseAndValidate(form);

        user.updateProfile(
                profile.displayName(),
                profile.age(),
                profile.bio()
        );

        return userRepository.save(user);
    }

    /**
     * FR_Profile_Keep_Ethics: requires the global ethical setting, a profile-specific opt-in, a checkbox, and the exact word DELETE.
     */
    @Transactional
    public void deactivateProfile(
            Long profileId,
            boolean confirmed,
            String deletionPhrase
    ) {
    public void deactivateProfile(
            Long profileId,
            boolean confirmed,
            String deletionPhrase
    ) {
        if (!profileSettings.isAccountDeletionEnabled()) {
            throw new ProfileException(
                    "Account deletion is currently disabled by Cupid's ethical settings."
            );
        }

        User user = requireActiveProfile(profileId);

        if (!user.isAccountDeactivationEnabled()) {
            throw new ProfileException(
                    "Enable account deletion in Account Settings before continuing."
                    "Account deletion is currently disabled by Cupid's ethical settings."
            );
        }

        User user = requireActiveProfile(profileId);

        if (!user.isAccountDeactivationEnabled()) {
            throw new ProfileException(
                    "Enable account deletion in Account Settings before continuing."
            );
        }

        if (!confirmed) {
            throw new ProfileException(
                    "Confirm that you understand the account-deletion warning."
            );
        }

        if (!"DELETE".equals(deletionPhrase == null ? "" : deletionPhrase.trim())) {
            throw new ProfileException(
                    "Type DELETE exactly to confirm account deletion."
            );
        }

        if (!"DELETE".equals(deletionPhrase == null ? "" : deletionPhrase.trim())) {
            throw new ProfileException(
                    "Type DELETE exactly to confirm account deletion."
            );
        }

        user.deactivate();
        userRepository.save(user);
    }

    /**
     * FR_Profile_Keep_Ethics: records the profile owner's deliberate choice to make the carefully confirmed action available.
     */
    @Transactional
    public void updateAccountDeactivationPreference(
            Long profileId,
            boolean enabled
    ) {
        User user = requireActiveProfile(profileId);
        user.updateAccountDeactivationPreference(enabled);
        userRepository.save(user);
    }

    /**
     * FR_Swipe_More_Ethics: records the profile owner's opt-in or opt-out of coercive swipe reminders. Persisted alongside the account deactivation preference so all ethics choices live in one place*
     */
    @Transactional
    public void updateSwipeEncouragementPreference(
            Long profileId,
            boolean enabled
    ) {
        User user = requireActiveProfile(profileId);
        user.updateSwipeEncouragementPreference(enabled);
        userRepository.save(user);
    }

    /**
     * FR_Message_More_Ethics: records the profile owner's opt-in or opt-out of coercive messaging pressure.
     */
    @Transactional
    public void updateMessageCoercionPreference(
            Long profileId,
            boolean enabled
    ) {
        User user = requireActiveProfile(profileId);
        user.updateMessageCoercionPreference(enabled);
        userRepository.save(user);
    }

    /**
     * Applies the non-functional discovery preferences (visibility, language,distance, age range) to the profile. Validates each field against the bounds the schema also enforces.
     */
    @Transactional
    public void updateDiscoveryPreferences(
            Long profileId,
            boolean showMeOnCupid,
            String preferredLanguage,
            int maxDistanceKm,
            int preferredMinAge,
            int preferredMaxAge
    ) {
        User user = requireActiveProfile(profileId);

        String cleanLanguage = preferredLanguage == null
                ? ""
                : preferredLanguage.trim();

        if (!SUPPORTED_LANGUAGES.contains(cleanLanguage)) {
            throw new ProfileException("Choose a supported language.");
        }

        if (maxDistanceKm < MIN_DISTANCE_KM || maxDistanceKm > MAX_DISTANCE_KM) {
            throw new ProfileException(
                    "Maximum distance must be between 1 and 500 kilometres."
            );
        }

        if (preferredMinAge < MIN_PREFERRED_AGE
                || preferredMinAge > MAX_PREFERRED_AGE
                || preferredMaxAge < MIN_PREFERRED_AGE
                || preferredMaxAge > MAX_PREFERRED_AGE) {
            throw new ProfileException(
                    "Preferred ages must be between 18 and 120."
            );
        }

        if (preferredMinAge > preferredMaxAge) {
            throw new ProfileException(
                    "Preferred minimum age cannot exceed the maximum."
            );
        }

        user.updateDiscoveryPreferences(
                showMeOnCupid,
                cleanLanguage,
                maxDistanceKm,
                preferredMinAge,
                preferredMaxAge
        );
        userRepository.save(user);
    }

    /**
     * Exposes the one global setting required by FR_Profile_Keep_Ethics.
     */
    public boolean isAccountDeletionEnabled() {
        return profileSettings.isAccountDeletionEnabled();
    }

    /**
     * Exposes the one global setting required by FR_Swipe_More_Ethics.
     * Controllers use this to reflect the app-wide toggle status on the
     * shared preferences page.
     */
    public boolean isSwipeEncouragementEnabled() {
        return matchingSettings.isSwipeEncouragementEnabled();
    }

    private User requireActiveProfile(Long profileId) {
        return findActiveProfile(profileId)
                .orElseThrow(() -> new ProfileException(
                        "The requested active profile could not be found."
                ));
    }

    private SanitisedProfile sanitiseAndValidate(ProfileForm form) {
        if (form == null || form.getAge() == null) {
            throw new ProfileException("A display name and age are required.");
        }

        String displayName = sanitise(form.getDisplayName());
        String bio = sanitise(form.getBio());

        if (displayName.isBlank()) {
            throw new ProfileException("Display name is required.");
        }

        if (displayName.length() > 100) {
            throw new ProfileException(
                    "Display name must be 100 characters or fewer."
            );
        }

        if (form.getAge() < 18 || form.getAge() > 120) {
            throw new ProfileException(
                    "Cupid profiles are available for ages 18 to 120."
            );
        }

        if (bio.length() > 500) {
            throw new ProfileException("Bio must be 500 characters or fewer.");
        }

        return new SanitisedProfile(displayName, form.getAge(), bio);
    }

    /**
     * Removes markup and control characters before persistence. Thymeleaf's
     * th:text also escapes output, providing defence in depth against XSS.
     */
    private String sanitise(String rawValue) {
        if (rawValue == null) {
            return "";
        }

        return rawValue
                .replaceAll("<[^>]*>", "")
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record SanitisedProfile(
            String displayName,
            int age,
            String bio
    ) {
    }
}
