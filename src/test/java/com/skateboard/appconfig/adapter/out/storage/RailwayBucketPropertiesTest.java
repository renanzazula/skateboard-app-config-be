package com.skateboard.appconfig.adapter.out.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RailwayBucketPropertiesTest {

    @Test
    void hasSensibleLocalDefaults() {
        RailwayBucketProperties properties = new RailwayBucketProperties();

        assertThat(properties.getBucketName()).isEqualTo("skateboard-branding");
        assertThat(properties.getEndpoint()).isEqualTo("http://localhost:9000");
        assertThat(properties.getAccessKeyId()).isEqualTo("minioadmin");
        assertThat(properties.getSecretAccessKey()).isEqualTo("minioadmin");
        assertThat(properties.getRegion()).isEqualTo("auto");
        assertThat(properties.getPresignedUrlExpirationMinutes()).isEqualTo(60);
    }

    @Test
    void everyFieldIsSettable() {
        RailwayBucketProperties properties = new RailwayBucketProperties();

        properties.setBucketName("my-bucket");
        properties.setEndpoint("https://s3.example.com");
        properties.setAccessKeyId("key-id");
        properties.setSecretAccessKey("secret");
        properties.setRegion("us-east-1");
        properties.setPresignedUrlExpirationMinutes(15);

        assertThat(properties.getBucketName()).isEqualTo("my-bucket");
        assertThat(properties.getEndpoint()).isEqualTo("https://s3.example.com");
        assertThat(properties.getAccessKeyId()).isEqualTo("key-id");
        assertThat(properties.getSecretAccessKey()).isEqualTo("secret");
        assertThat(properties.getRegion()).isEqualTo("us-east-1");
        assertThat(properties.getPresignedUrlExpirationMinutes()).isEqualTo(15);
    }
}
