package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.BrandingAsset;

import java.util.Arrays;
import java.util.Objects;

public interface UploadBrandingAssetUseCase {

    record Command(String adminId, String name, byte[] data, String mimeType) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Command other)) return false;
            return Objects.equals(adminId, other.adminId)
                    && Objects.equals(name, other.name)
                    && Arrays.equals(data, other.data)
                    && Objects.equals(mimeType, other.mimeType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(adminId, name, mimeType);
            return 31 * result + Arrays.hashCode(data);
        }

        @Override
        public String toString() {
            return "Command[adminId=" + adminId + ", name=" + name
                    + ", data=byte[" + (data == null ? 0 : data.length) + "], mimeType=" + mimeType + "]";
        }
    }

    BrandingAsset execute(Command command);
}
