package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

import java.util.Arrays;
import java.util.Objects;

public interface UploadLoginBackgroundUseCase {

    record Command(String adminId, byte[] data, String mimeType) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Command other)) return false;
            return Objects.equals(adminId, other.adminId)
                    && Arrays.equals(data, other.data)
                    && Objects.equals(mimeType, other.mimeType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(adminId, mimeType);
            return 31 * result + Arrays.hashCode(data);
        }

        @Override
        public String toString() {
            return "Command[adminId=" + adminId + ", data=byte[" + (data == null ? 0 : data.length)
                    + "], mimeType=" + mimeType + "]";
        }
    }

    AppConfig execute(Command command);
}
