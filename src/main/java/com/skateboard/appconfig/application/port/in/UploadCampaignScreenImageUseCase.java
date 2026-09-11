package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.CampaignScreen;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public interface UploadCampaignScreenImageUseCase {

    record Command(String adminId, UUID campaignId, UUID screenId, byte[] data, String mimeType,
                   Double focalPointX, Double focalPointY) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Command other)) return false;
            return Objects.equals(adminId, other.adminId)
                    && Objects.equals(campaignId, other.campaignId)
                    && Objects.equals(screenId, other.screenId)
                    && Arrays.equals(data, other.data)
                    && Objects.equals(mimeType, other.mimeType)
                    && Objects.equals(focalPointX, other.focalPointX)
                    && Objects.equals(focalPointY, other.focalPointY);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(adminId, campaignId, screenId, mimeType, focalPointX, focalPointY);
            return 31 * result + Arrays.hashCode(data);
        }

        @Override
        public String toString() {
            return "Command[adminId=" + adminId + ", campaignId=" + campaignId + ", screenId=" + screenId
                    + ", data=byte[" + (data == null ? 0 : data.length) + "], mimeType=" + mimeType
                    + ", focalPointX=" + focalPointX + ", focalPointY=" + focalPointY + "]";
        }
    }

    CampaignScreen execute(Command command);
}
