package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface UploadLoginBackgroundUseCase {

    record Command(String adminId, byte[] data, String mimeType) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Command other)) return false;
            return ImageUploadCommandSupport.equals(adminId, data, mimeType, other.adminId, other.data, other.mimeType);
        }

        @Override
        public int hashCode() {
            return ImageUploadCommandSupport.hashCode(adminId, data, mimeType);
        }

        @Override
        public String toString() {
            return ImageUploadCommandSupport.toString("Command", adminId, data, mimeType);
        }
    }

    AppConfig execute(Command command);
}
