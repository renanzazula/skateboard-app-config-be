package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.AddCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.in.ArchiveCampaignUseCase;
import com.skateboard.appconfig.application.port.in.CreateCampaignUseCase;
import com.skateboard.appconfig.application.port.in.DeleteCampaignUseCase;
import com.skateboard.appconfig.application.port.in.GetActiveCampaignsUseCase;
import com.skateboard.appconfig.application.port.in.GetCampaignUseCase;
import com.skateboard.appconfig.application.port.in.ListCampaignsUseCase;
import com.skateboard.appconfig.application.port.in.PauseCampaignUseCase;
import com.skateboard.appconfig.application.port.in.PublishCampaignUseCase;
import com.skateboard.appconfig.application.port.in.RecordCampaignEventUseCase;
import com.skateboard.appconfig.application.port.in.RemoveCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.in.ReorderCampaignScreensUseCase;
import com.skateboard.appconfig.application.port.in.UpdateCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
import com.skateboard.appconfig.application.port.in.UploadCampaignScreenImageUseCase;
import com.skateboard.appconfig.domain.model.CampaignEventType;
import com.skateboard.appconfig.infrastructure.web.api.CampaignApi;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignEventRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRuntimeResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenResponse;
import com.skateboard.appconfig.infrastructure.web.dto.ReorderCampaignScreensRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * All 15 campaign endpoints (spec §15). {@code GET /api/campaigns/active} and
 * {@code POST /api/campaigns/{id}/events} are pre-auth (see {@code SecurityConfig});
 * every admin operation is gated per-method by {@code FUNC_CAMPAIGN_READ}
 * (reads), {@code FUNC_CAMPAIGN_MANAGE} (create/edit/screens) or
 * {@code FUNC_CAMPAIGN_PUBLISH} (publish/pause/archive), same style as
 * {@code BrandingAdminController}. Presigning happens in {@link CampaignWebMapper},
 * never in the domain/service layer.
 * <p>
 * One controller implements the whole generated {@code CampaignApi} (the tag
 * covers both runtime and admin), matching {@code AboutUsController}.
 */
@RestController
public class CampaignController implements CampaignApi {

    private final ListCampaignsUseCase listCampaignsUseCase;
    private final GetCampaignUseCase getCampaignUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignUseCase updateCampaignUseCase;
    private final DeleteCampaignUseCase deleteCampaignUseCase;
    private final PublishCampaignUseCase publishCampaignUseCase;
    private final PauseCampaignUseCase pauseCampaignUseCase;
    private final ArchiveCampaignUseCase archiveCampaignUseCase;
    private final AddCampaignScreenUseCase addCampaignScreenUseCase;
    private final UpdateCampaignScreenUseCase updateCampaignScreenUseCase;
    private final RemoveCampaignScreenUseCase removeCampaignScreenUseCase;
    private final ReorderCampaignScreensUseCase reorderCampaignScreensUseCase;
    private final UploadCampaignScreenImageUseCase uploadCampaignScreenImageUseCase;
    private final GetActiveCampaignsUseCase getActiveCampaignsUseCase;
    private final RecordCampaignEventUseCase recordCampaignEventUseCase;
    private final CampaignWebMapper mapper;

    public CampaignController(ListCampaignsUseCase listCampaignsUseCase,
                              GetCampaignUseCase getCampaignUseCase,
                              CreateCampaignUseCase createCampaignUseCase,
                              UpdateCampaignUseCase updateCampaignUseCase,
                              DeleteCampaignUseCase deleteCampaignUseCase,
                              PublishCampaignUseCase publishCampaignUseCase,
                              PauseCampaignUseCase pauseCampaignUseCase,
                              ArchiveCampaignUseCase archiveCampaignUseCase,
                              AddCampaignScreenUseCase addCampaignScreenUseCase,
                              UpdateCampaignScreenUseCase updateCampaignScreenUseCase,
                              RemoveCampaignScreenUseCase removeCampaignScreenUseCase,
                              ReorderCampaignScreensUseCase reorderCampaignScreensUseCase,
                              UploadCampaignScreenImageUseCase uploadCampaignScreenImageUseCase,
                              GetActiveCampaignsUseCase getActiveCampaignsUseCase,
                              RecordCampaignEventUseCase recordCampaignEventUseCase,
                              CampaignWebMapper mapper) {
        this.listCampaignsUseCase = listCampaignsUseCase;
        this.getCampaignUseCase = getCampaignUseCase;
        this.createCampaignUseCase = createCampaignUseCase;
        this.updateCampaignUseCase = updateCampaignUseCase;
        this.deleteCampaignUseCase = deleteCampaignUseCase;
        this.publishCampaignUseCase = publishCampaignUseCase;
        this.pauseCampaignUseCase = pauseCampaignUseCase;
        this.archiveCampaignUseCase = archiveCampaignUseCase;
        this.addCampaignScreenUseCase = addCampaignScreenUseCase;
        this.updateCampaignScreenUseCase = updateCampaignScreenUseCase;
        this.removeCampaignScreenUseCase = removeCampaignScreenUseCase;
        this.reorderCampaignScreensUseCase = reorderCampaignScreensUseCase;
        this.uploadCampaignScreenImageUseCase = uploadCampaignScreenImageUseCase;
        this.getActiveCampaignsUseCase = getActiveCampaignsUseCase;
        this.recordCampaignEventUseCase = recordCampaignEventUseCase;
        this.mapper = mapper;
    }

    // ---- runtime (pre-auth) ----

    @Override
    public ResponseEntity<List<CampaignRuntimeResponse>> getActiveCampaigns() {
        List<CampaignRuntimeResponse> body = getActiveCampaignsUseCase.execute(isAuthenticated()).stream()
                .map(mapper::toRuntimeResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Void> recordCampaignEvent(UUID campaignId, CampaignEventRequest request) {
        recordCampaignEventUseCase.execute(new RecordCampaignEventUseCase.Command(
                campaignId, request.getScreenId(), toDomainEventType(request), request.getPlatform(),
                request.getAppVersion(), request.getActionTarget()));
        return ResponseEntity.noContent().build();
    }

    // ---- admin: reads ----

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_READ')")
    public ResponseEntity<List<CampaignResponse>> listCampaigns() {
        return ResponseEntity.ok(listCampaignsUseCase.execute().stream().map(mapper::toResponse).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_READ')")
    public ResponseEntity<CampaignResponse> getCampaign(UUID campaignId) {
        return ResponseEntity.ok(mapper.toResponse(getCampaignUseCase.execute(campaignId)));
    }

    // ---- admin: create / edit ----

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignResponse> createCampaign(CampaignRequest request) {
        return ResponseEntity.status(201).body(mapper.toResponse(
                createCampaignUseCase.execute(mapper.toCreateCommand(currentAdminId(), request))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignResponse> updateCampaign(UUID campaignId, CampaignRequest request) {
        return ResponseEntity.ok(mapper.toResponse(
                updateCampaignUseCase.execute(mapper.toUpdateCommand(currentAdminId(), campaignId, request))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<Void> deleteCampaign(UUID campaignId) {
        deleteCampaignUseCase.execute(campaignId);
        return ResponseEntity.noContent().build();
    }

    // ---- admin: lifecycle ----

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_PUBLISH')")
    public ResponseEntity<CampaignResponse> publishCampaign(UUID campaignId) {
        return ResponseEntity.ok(mapper.toResponse(
                publishCampaignUseCase.execute(new PublishCampaignUseCase.Command(currentAdminId(), campaignId))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_PUBLISH')")
    public ResponseEntity<CampaignResponse> pauseCampaign(UUID campaignId) {
        return ResponseEntity.ok(mapper.toResponse(
                pauseCampaignUseCase.execute(new PauseCampaignUseCase.Command(currentAdminId(), campaignId))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_PUBLISH')")
    public ResponseEntity<CampaignResponse> archiveCampaign(UUID campaignId) {
        return ResponseEntity.ok(mapper.toResponse(
                archiveCampaignUseCase.execute(new ArchiveCampaignUseCase.Command(currentAdminId(), campaignId))));
    }

    // ---- admin: screens ----

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignScreenResponse> addCampaignScreen(UUID campaignId, CampaignScreenRequest request) {
        return ResponseEntity.status(201).body(mapper.toScreenResponse(
                addCampaignScreenUseCase.execute(new AddCampaignScreenUseCase.Command(
                        currentAdminId(), campaignId, mapper.toDraft(request)))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignScreenResponse> updateCampaignScreen(UUID campaignId, UUID screenId,
                                                                       CampaignScreenRequest request) {
        return ResponseEntity.ok(mapper.toScreenResponse(
                updateCampaignScreenUseCase.execute(new UpdateCampaignScreenUseCase.Command(
                        currentAdminId(), campaignId, screenId, mapper.toDraft(request)))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<Void> removeCampaignScreen(UUID campaignId, UUID screenId) {
        removeCampaignScreenUseCase.execute(
                new RemoveCampaignScreenUseCase.Command(currentAdminId(), campaignId, screenId));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignResponse> reorderCampaignScreens(UUID campaignId,
                                                                   ReorderCampaignScreensRequest request) {
        return ResponseEntity.ok(mapper.toResponse(reorderCampaignScreensUseCase.execute(
                new ReorderCampaignScreensUseCase.Command(currentAdminId(), campaignId, request.getScreenIds()))));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_CAMPAIGN_MANAGE')")
    public ResponseEntity<CampaignScreenResponse> uploadCampaignScreenImage(UUID campaignId, UUID screenId,
                                                                            MultipartFile file, Float focalPointX,
                                                                            Float focalPointY) {
        return ResponseEntity.ok(mapper.toScreenResponse(uploadCampaignScreenImageUseCase.execute(
                new UploadCampaignScreenImageUseCase.Command(currentAdminId(), campaignId, screenId, readBytes(file),
                        file.getContentType(), toDouble(focalPointX), toDouble(focalPointY)))));
    }

    // ---- helpers ----

    private static CampaignEventType toDomainEventType(CampaignEventRequest request) {
        return request.getEventType() == null ? null : CampaignEventType.valueOf(request.getEventType().getValue());
    }

    private static Double toDouble(Float value) {
        return value == null ? null : value.doubleValue();
    }

    private static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    private static String currentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
