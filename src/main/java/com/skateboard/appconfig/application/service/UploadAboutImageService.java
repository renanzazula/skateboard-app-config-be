package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadAboutImageUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Stores an About Us content image under {@code about-us/<uuid><ext>} and
 * returns a signed URL for it. The block that references it persists only the
 * bare key ({@code AboutBlockImageResolver} strips the signature back off on
 * save) — the same size/MIME rules as the branding uploads
 * ({@link ImageUploadValidator}).
 */
@Service
public class UploadAboutImageService implements UploadAboutImageUseCase {

    static final String KEY_PREFIX = "about-us/";

    private final ObjectStoragePort objectStoragePort;

    public UploadAboutImageService(ObjectStoragePort objectStoragePort) {
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    public String execute(Command command) {
        String extension = ImageUploadValidator.extensionFor(command.data(), command.mimeType());
        String key = KEY_PREFIX + UUID.randomUUID() + extension;
        objectStoragePort.put(key, command.data(), command.mimeType());
        return objectStoragePort.presignGetUrl(key);
    }
}
