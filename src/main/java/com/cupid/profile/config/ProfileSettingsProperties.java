package com.cupid.profile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * One shared, configurable ethical setting for Profile operations.
 *
 * Requirement: FR_Profile_Keep_Ethics.
 *
 * The default protects users from accidental account deletion. Tanzim's
 * settings/preferences UI should use this property contract rather than
 * duplicating the deletion policy in a controller or template.
 */
@ConfigurationProperties(prefix = "cupid.profile")
public class ProfileSettingsProperties {

    private boolean accountDeletionEnabled = false;

    public boolean isAccountDeletionEnabled() {
        return accountDeletionEnabled;
    }

    public void setAccountDeletionEnabled(boolean accountDeletionEnabled) {
        this.accountDeletionEnabled = accountDeletionEnabled;
    }
}
