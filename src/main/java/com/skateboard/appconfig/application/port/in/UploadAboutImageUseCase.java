package com.skateboard.appconfig.application.port.in;

public interface UploadAboutImageUseCase {

    record Command(byte[] data, String mimeType) {}

    /** Stores the image and returns a freshly signed URL for immediate preview. */
    String execute(Command command);
}
