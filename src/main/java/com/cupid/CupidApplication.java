package com.cupid;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.cupid.profile.picture.config.ProfilePictureProperties;

/**
 * Application composition root.
 *
 * Architecture: Spring scans the shared {@code com.cupid} namespace so the
 * Profile, Matching, Messaging, and Picture components can collaborate while
 * retaining separate controller and service responsibilities. Configuration
 * classes are listed explicitly to keep assessment-critical ethical and upload
 * settings outside business code.
 */
@SpringBootApplication
@EnableConfigurationProperties({
    MatchingProperties.class,
    ProfileSettingsProperties.class,
    ProfilePictureProperties.class
})
public class CupidApplication {

    public static void main(String[] args) {
        SpringApplication.run(CupidApplication.class, args);
    }
}
