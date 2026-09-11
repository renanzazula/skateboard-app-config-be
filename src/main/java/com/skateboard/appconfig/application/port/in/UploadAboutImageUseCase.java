package com.skateboard.appconfig.application.port.in;

import java.util.Arrays;
import java.util.Objects;

public interface UploadAboutImageUseCase {

    record Command(byte[] data, String mimeType) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Command other)) return false;
            return Arrays.equals(data, other.data) && Objects.equals(mimeType, other.mimeType);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(data) + Objects.hashCode(mimeType);
        }

        @Override
        public String toString() {
            return "Command[data=byte[" + (data == null ? 0 : data.length) + "], mimeType=" + mimeType + "]";
        }
    }

    /** Stores the image and returns a freshly signed URL for immediate preview. */
    String execute(Command command);
}
