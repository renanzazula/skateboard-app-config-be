package com.skateboard.appconfig.adapter.out.storage;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Building the {@code S3Client}/{@code S3Presigner} beans only configures an
 * SDK client object (endpoint, region, credentials, path-style access); it
 * performs no network I/O, so this can run as a plain unit test.
 */
class S3StorageConfigTest {

    private final S3StorageConfig config = new S3StorageConfig();

    @Test
    void buildsAnS3ClientPointedAtTheConfiguredEndpoint() {
        RailwayBucketProperties properties = new RailwayBucketProperties();

        try (S3Client client = config.s3Client(properties)) {
            assertThat(client).isNotNull();
        }
    }

    @Test
    void buildsAnS3PresignerPointedAtTheConfiguredEndpoint() {
        RailwayBucketProperties properties = new RailwayBucketProperties();

        try (S3Presigner presigner = config.s3Presigner(properties)) {
            assertThat(presigner).isNotNull();
        }
    }
}
