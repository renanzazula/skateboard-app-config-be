package com.skateboard.appconfig.application.port.out;

public interface ObjectStoragePort {
    void put(String key, byte[] data, String contentType);
    String presignGetUrl(String key);
    void delete(String key);
}
