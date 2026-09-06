package com.skateboard.appconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling drives CampaignEventRetentionJob (the campaign_event table
// has no other cleanup). No other scheduled work exists in this service today.
@EnableScheduling
@SpringBootApplication
public class AppConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppConfigApplication.class, args);
    }
}
