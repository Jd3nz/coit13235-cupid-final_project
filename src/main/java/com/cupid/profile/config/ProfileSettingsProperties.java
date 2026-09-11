package com.cupid.profile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global ethical control for account deletion.
 *
 * FR_Profile_Keep_Ethics requires one setting that can disable this
 * functionality for every Cupid user.
 */
@ConfigurationProperties(prefix = "cupid.profile")
public class ProfileSettingsProperties {

    private boolean accountDeletionEnabled = true;

    public boolean isAccountDeletionEnabled() {
        return accountDeletionEnabled;
    }

    public void setAccountDeletionEnabled(boolean accountDeletionEnabled) {
        this.accountDeletionEnabled = accountDeletionEnabled;
    }
}
