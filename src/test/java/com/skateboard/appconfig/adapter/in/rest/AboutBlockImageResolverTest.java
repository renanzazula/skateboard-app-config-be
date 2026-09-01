package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;

class AboutBlockImageResolverTest {

    @Mock
    private ObjectStoragePort objectStoragePort;

    private AboutBlockImageResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(objectStoragePort.presignGetUrl(startsWith("about-us/")))
                .thenAnswer(inv -> "https://cdn.example/bucket/" + inv.getArgument(0) + "?sig=abc");
        resolver = new AboutBlockImageResolver(objectStoragePort);
    }

    @Test
    void signsBareKeysOnRead() {
        List<Map<String, Object>> blocks = List.of(
                Map.of("type", "hero", "data", Map.of("imageUrl", "about-us/1234.jpg")),
                Map.of("type", "gallery", "data", Map.of("urls",
                        List.of("about-us/a.png", "https://external.example/x.jpg"))));

        List<Map<String, Object>> signed = resolver.toSignedUrls(blocks);

        assertThat(nested(signed.get(0), "data", "imageUrl"))
                .isEqualTo("https://cdn.example/bucket/about-us/1234.jpg?sig=abc");
        @SuppressWarnings("unchecked")
        List<String> urls = (List<String>) ((Map<String, Object>) signed.get(1).get("data")).get("urls");
        assertThat(urls.get(0)).startsWith("https://cdn.example/bucket/about-us/a.png");
        assertThat(urls.get(1)).isEqualTo("https://external.example/x.jpg");
    }

    @Test
    void stripsSignaturesBackToKeysOnSave() {
        List<Map<String, Object>> blocks = List.of(
                Map.of("type", "hero", "data", Map.of(
                        "imageUrl", "https://cdn.example/bucket/about-us/1234.jpg?sig=stale&X-Amz-Date=z")),
                Map.of("type", "image", "data", Map.of("url", "https://external.example/keep.jpg")));

        List<Map<String, Object>> keys = resolver.toStorageKeys(blocks);

        assertThat(nested(keys.get(0), "data", "imageUrl")).isEqualTo("about-us/1234.jpg");
        assertThat(nested(keys.get(1), "data", "url")).isEqualTo("https://external.example/keep.jpg");
    }

    @Test
    void leavesPlainTextUntouched() {
        List<Map<String, Object>> blocks = List.of(
                Map.of("type", "text", "data", Map.of("html", "We started skating in about-us times.")));

        assertThat(nested(resolver.toStorageKeys(blocks).get(0), "data", "html"))
                .isEqualTo("We started skating in about-us times.");
    }

    @SuppressWarnings("unchecked")
    private static Object nested(Map<String, Object> block, String a, String b) {
        return ((Map<String, Object>) block.get(a)).get(b);
    }
}
