package com.cupid;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.profile.config.ProfileSettingsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// Scan each team-owned component beneath the shared com.cupid namespace.
@SpringBootApplication
@EnableConfigurationProperties({
        MatchingProperties.class,
        ProfileSettingsProperties.class
})
public class CupidMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CupidMatchingApplication.class, args);
    }
}

