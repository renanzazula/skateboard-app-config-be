package com.skateboard.appconfig.application.service;

import java.util.Map;

/**
 * Shared validation for the three image-upload flows (login background, app
 * logo, branding asset) — same size/MIME rules everywhere per the migration
 * guide, so it's centralized instead of repeated per use case.
 */
final class ImageUploadValidator {

    static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_MIME_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private ImageUploadValidator() {}

    static String extensionFor(byte[] data, String mimeType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File is required.");
        }
        if (data.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must be lower than 5 MB.");
        }
        String extension = ALLOWED_MIME_EXTENSIONS.get(mimeType);
        if (extension == null) {
            throw new IllegalArgumentException("Unsupported image type: " + mimeType);
        }
        return extension;
    }
}
