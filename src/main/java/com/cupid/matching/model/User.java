package com.cupid.matching.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Represents the minimum user information required by
 * the Cupid Matching component.
 *
 * Architecture: this is the shared profile entity used by Profile, Matching,
 * Messaging, and Picture components. It keeps cross-component preferences in
 * one table while each service owns its validation and workflow rules.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Column(nullable = false)
    private int age;
    @Column(length = 500)
    private String bio;
    // Soft deletion retains records needed for swipe, match, and message history.
    @Column(nullable = false)
    private boolean active;
    // FR_Profile_Keep_Ethics: per-profile opt-in before the protected delete flow.
    @Column(name = "account_deactivation_enabled", nullable = false)
    private boolean accountDeactivationEnabled;
    // FR_Swipe_More_Ethics: individual opt-out layered under the app-wide switch.
    @Column(name = "swipe_encouragement_enabled", nullable = false)
    private boolean swipeEncouragementEnabled = true;
    // FR_Message_More_Ethics: individual opt-out for messaging reminders.
    @Column(name = "message_coercion_enabled", nullable = false)
    private boolean messageCoercionEnabled = true;
    // Stored demo defaults; Matching does not apply them to suggestions yet.
    @Column(name = "show_me_on_cupid", nullable = false)
    private boolean showMeOnCupid = true;
    @Column(name = "preferred_language", nullable = false, length = 30)
    private String preferredLanguage = "English";
    @Column(name = "max_distance_km", nullable = false)
    private int maxDistanceKm = 80;
    @Column(name = "preferred_min_age", nullable = false)
    private int preferredMinAge = 18;
    @Column(name = "preferred_max_age", nullable = false)
    private int preferredMaxAge = 60;
    // Database-managed creation time provides persistent profile traceability.
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected User() {
        // Required by JPA, application callers retain the full constructor.
    }

    public User(
            Long id,
            String displayName,
            int age,
            String bio,
            boolean active,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.displayName = displayName;
        this.age = age;
        this.bio = bio;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAge() {
        return age;
    }

    public String getBio() {
        return bio;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * FR_Profile_Keep_Ethics: this per-profile preference starts false so
     * deactivation takes an intentional user-controlled extra step
     */
    public boolean isAccountDeactivationEnabled() {
        return accountDeactivationEnabled;
    }

    /**
     * FR_Swipe_More_Ethics: per-user opt-out for coercive swipe reminders
     * Defaults to true so the app-wide setting is the single point of control. a user can then choose to opt out for themselves
     */
    public boolean isSwipeEncouragementEnabled() {
        return swipeEncouragementEnabled;
    }

    /**
     * FR_Message_More_Ethics: per-user opt-out for coercive messaging pressure. Semantically mirrors swipeEncouragementEnabled so the shared preferences page can present both toggles consistently
     */
    public boolean isMessageCoercionEnabled() {
        return messageCoercionEnabled;
    }

    /**
     * Non-functional preference: controls whether this profile appears in other users' discovery feed. A soft toggle that leaves matches and conversations intact
     */
    public boolean isShowMeOnCupid() {
        return showMeOnCupid;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public int getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public int getPreferredMinAge() {
        return preferredMinAge;
    }

    public int getPreferredMaxAge() {
        return preferredMaxAge;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * FR_Profile: Changes the information that a person has chosen to show on their Cupid profile. Validation and input sanitisation happen in ProfileService before this domain operation is called.
     */
    public void updateProfile(
            String displayName,
            int age,
            String bio
    ) {
        this.displayName = displayName;
        this.age = age;
        this.bio = bio;
    }

    /**
     * FR_Profile_Keep_Ethics: performs a soft account deletion. Keeping therecord protects swipe and match history while removing the account from all active-profile and discovery queries.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Records the account owner's preference from the Account Settings page.
     */
    public void updateAccountDeactivationPreference(boolean enabled) {
        this.accountDeactivationEnabled = enabled;
    }

    /**
     * FR_Swipe_More_Ethics: records the account owner's optin or opt-out for coercive swipe encouragement.
     */
    public void updateSwipeEncouragementPreference(boolean enabled) {
        this.swipeEncouragementEnabled = enabled;
    }

    /**
     * FR_Message_More_Ethics: records the account owner's opt-in or opt-out for coercive messaging pressure
     */
    public void updateMessageCoercionPreference(boolean enabled) {
        this.messageCoercionEnabled = enabled;
    }

    /**
     * Applies the non-functional discovery preferences to this profile.
     * Validation happens in ProfileService before this method is invoked.
     */
    public void updateDiscoveryPreferences(
            boolean showMeOnCupid,
            String preferredLanguage,
            int maxDistanceKm,
            int preferredMinAge,
            int preferredMaxAge
    ) {
        this.showMeOnCupid = showMeOnCupid;
        this.preferredLanguage = preferredLanguage;
        this.maxDistanceKm = maxDistanceKm;
        this.preferredMinAge = preferredMinAge;
        this.preferredMaxAge = preferredMaxAge;
    }
}
