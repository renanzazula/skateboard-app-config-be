package com.skateboard.appconfig.application.port.in;

import java.util.Arrays;
import java.util.Objects;

/**
 * {@code equals}/{@code hashCode}/{@code toString} shared by every upload
 * {@code Command} record shaped {@code (adminId, byte[] data, mimeType)} —
 * {@link UploadAppLogoUseCase.Command} and
 * {@link UploadLoginBackgroundUseCase.Command} today. Each record still
 * hand-writes its own overrides (records can't extend a common base, and
 * each belongs to its own use-case interface per this codebase's one-port-
 * per-use-case convention) — this only centralizes the logic those overrides
 * delegate to, since a record-generated equals/hashCode would compare the
 * byte[] by reference.
 */
final class ImageUploadCommandSupport {

    private ImageUploadCommandSupport() {
    }

    static boolean equals(String adminId, byte[] data, String mimeType,
                           String otherAdminId, byte[] otherData, String otherMimeType) {
        return Objects.equals(adminId, otherAdminId)
                && Arrays.equals(data, otherData)
                && Objects.equals(mimeType, otherMimeType);
    }

    static int hashCode(String adminId, byte[] data, String mimeType) {
        int result = Objects.hash(adminId, mimeType);
        return 31 * result + Arrays.hashCode(data);
    }

    static String toString(String recordName, String adminId, byte[] data, String mimeType) {
        return recordName + "[adminId=" + adminId + ", data=byte[" + (data == null ? 0 : data.length)
                + "], mimeType=" + mimeType + "]";
    }
}
