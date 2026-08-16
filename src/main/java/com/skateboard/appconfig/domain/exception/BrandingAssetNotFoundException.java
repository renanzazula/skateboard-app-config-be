package com.skateboard.appconfig.domain.exception;

public class BrandingAssetNotFoundException extends RuntimeException {

    public BrandingAssetNotFoundException(String id) {
        super("Branding asset not found: " + id);
    }
}
