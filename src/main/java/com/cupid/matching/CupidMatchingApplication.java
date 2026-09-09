package com.cupid.matching;

import com.cupid.matching.config.MatchingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MatchingProperties.class)
public class CupidMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CupidMatchingApplication.class, args);
    }
}