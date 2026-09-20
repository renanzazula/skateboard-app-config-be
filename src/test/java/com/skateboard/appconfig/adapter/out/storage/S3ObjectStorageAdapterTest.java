package com.skateboard.appconfig.adapter.out.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ObjectStorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedRequest;

    private S3ObjectStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RailwayBucketProperties properties = new RailwayBucketProperties();
        properties.setBucketName("test-bucket");
        adapter = new S3ObjectStorageAdapter(s3Client, s3Presigner, properties);
    }

    @Test
    void putUploadsTheObjectWithTheGivenContentType() {
        adapter.put("assets/logo.png", new byte[]{1, 2, 3}, "image/png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().key()).isEqualTo("assets/logo.png");
        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
        assertThat(captor.getValue().contentLength()).isEqualTo(3L);
    }

    @Test
    void presignGetUrlReturnsNullForABlankKey() {
        assertThat(adapter.presignGetUrl(null)).isNull();
        assertThat(adapter.presignGetUrl("  ")).isNull();
    }

    @Test
    void presignGetUrlReturnsTheSignedUrl() throws Exception {
        when(presignedRequest.url()).thenReturn(URI.create("https://signed.example.com/assets/logo.png").toURL());
        when(s3Presigner.presignGetObject(any(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        String url = adapter.presignGetUrl("assets/logo.png");

        assertThat(url).isEqualTo("https://signed.example.com/assets/logo.png");
    }

    @Test
    void deleteSkipsABlankKey() {
        adapter.delete(null);
        adapter.delete(" ");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteRemovesTheObject() {
        adapter.delete("assets/logo.png");

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().key()).isEqualTo("assets/logo.png");
    }

    @Test
    void deleteSwallowsAnSdkFailure() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(new RuntimeException("boom"));

        adapter.delete("assets/logo.png");
    }
}
