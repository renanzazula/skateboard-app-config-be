package com.skateboard.appconfig.domain.exception;

public class BrandingAssetNameConflictException extends RuntimeException {

    public BrandingAssetNameConflictException(String name) {
        super("A branding asset named '" + name + "' already exists");
    }
}
