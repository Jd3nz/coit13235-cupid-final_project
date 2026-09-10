package com.cupid.matching.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for requirement FR_Swipe_More_Ethics.
 *
 * The encouragement feature can be changed without modifying Java code.
 */
@ConfigurationProperties(prefix = "cupid.matching")
public class MatchingProperties {

    private boolean swipeEncouragementEnabled = false;

    public boolean isSwipeEncouragementEnabled() {
        return swipeEncouragementEnabled;
    }

    public void setSwipeEncouragementEnabled(boolean swipeEncouragementEnabled) {
        this.swipeEncouragementEnabled = swipeEncouragementEnabled;
    }
}
