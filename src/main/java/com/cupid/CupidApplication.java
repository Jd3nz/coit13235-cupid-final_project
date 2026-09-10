package com.cupid;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.cupid.profile.picture.config.ProfilePictureProperties;

// Scan each team-owned component beneath the shared com.cupid namespace.
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
