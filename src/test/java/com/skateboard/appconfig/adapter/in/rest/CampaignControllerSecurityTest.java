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
import com.skateboard.appconfig.infrastructure.security.SecurityConfig;
import com.skateboard.appconfig.infrastructure.web.GlobalExceptionHandler;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This service's own authorization gate for the campaign routes — the last line
 * of defence, since the BFF's matching {@code @PreAuthorize} checks are a
 * duplicate and a token can reach this service without passing through it.
 * <p>
 * Two routes are deliberately pre-auth ({@code GET /api/campaigns/active} and
 * {@code POST /api/campaigns/{id}/events}); every {@code /api/campaigns/admin/**}
 * route requires the specific {@code FUNC_CAMPAIGN_*} authority from the spec's
 * {@code x-required-permissions}, and holding one of the three must not imply
 * the others.
 */
@WebMvcTest(controllers = CampaignController.class)
// @EnableMethodSecurity proxies the controller for its @PreAuthorize checks, and
// CampaignController implements the generated CampaignApi — so without Boot's
// AopAutoConfiguration (not part of the @WebMvcTest slice) it would be JDK-proxied
// to the interface, the class-level @RestController would be invisible to
// RequestMappingHandlerMapping and every route would 404. Production gets CGLIB
// via spring.aop.proxy-target-class=true; import the same auto-configuration here.
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, CampaignControllerSecurityTest.MetricsStub.class})
class CampaignControllerSecurityTest {

    /**
     * CampaignEventRateLimitFilter is a servlet Filter, so the @WebMvcTest slice
     * instantiates it, but Micrometer's registry auto-configuration is not part
     * of the slice — supply an in-memory one rather than excluding the filter,
     * so the pre-auth event route is exercised through it here too.
     */
    @TestConfiguration
    static class MetricsStub {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SCREEN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ListCampaignsUseCase listCampaignsUseCase;
    @MockBean private GetCampaignUseCase getCampaignUseCase;
    @MockBean private CreateCampaignUseCase createCampaignUseCase;
    @MockBean private UpdateCampaignUseCase updateCampaignUseCase;
    @MockBean private DeleteCampaignUseCase deleteCampaignUseCase;
    @MockBean private PublishCampaignUseCase publishCampaignUseCase;
    @MockBean private PauseCampaignUseCase pauseCampaignUseCase;
    @MockBean private ArchiveCampaignUseCase archiveCampaignUseCase;
    @MockBean private AddCampaignScreenUseCase addCampaignScreenUseCase;
    @MockBean private UpdateCampaignScreenUseCase updateCampaignScreenUseCase;
    @MockBean private RemoveCampaignScreenUseCase removeCampaignScreenUseCase;
    @MockBean private ReorderCampaignScreensUseCase reorderCampaignScreensUseCase;
    @MockBean private UploadCampaignScreenImageUseCase uploadCampaignScreenImageUseCase;
    @MockBean private GetActiveCampaignsUseCase getActiveCampaignsUseCase;
    @MockBean private RecordCampaignEventUseCase recordCampaignEventUseCase;
    @MockBean private CampaignWebMapper mapper;

    // ---- the two pre-auth runtime routes ----

    @Test
    void activeCampaignsAreServedWithoutAToken() throws Exception {
        given(getActiveCampaignsUseCase.execute(anyBoolean())).willReturn(List.of());

        mockMvc.perform(get("/api/campaigns/active"))
                .andExpect(status().isOk());
    }

    @Test
    void anAnonymousSessionIsReportedAsUnauthenticatedSoItOnlySeesItsOwnAudiences() throws Exception {
        given(getActiveCampaignsUseCase.execute(anyBoolean())).willReturn(List.of());

        mockMvc.perform(get("/api/campaigns/active")).andExpect(status().isOk());

        verify(getActiveCampaignsUseCase).execute(false);
    }

    @Test
    void aSignedInSessionIsReportedAsAuthenticated() throws Exception {
        given(getActiveCampaignsUseCase.execute(anyBoolean())).willReturn(List.of());

        mockMvc.perform(get("/api/campaigns/active").with(jwt()))
                .andExpect(status().isOk());

        verify(getActiveCampaignsUseCase).execute(true);
    }

    @Test
    void anEventIsAcceptedWithoutAToken() throws Exception {
        mockMvc.perform(post("/api/campaigns/{id}/events", CAMPAIGN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"CAMPAIGN_STARTED\"}"))
                .andExpect(status().isNoContent());

        verify(recordCampaignEventUseCase).execute(any());
    }

    // ---- admin reads require FUNC_CAMPAIGN_READ ----

    @Test
    void theAdminListIsRejectedWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/campaigns/admin"))
                .andExpect(status().isUnauthorized());

        verify(listCampaignsUseCase, never()).execute();
    }

    @Test
    void theAdminListIsRejectedForATokenWithoutFuncCampaignRead() throws Exception {
        mockMvc.perform(get("/api/campaigns/admin")
                        .with(jwt().authorities(new SimpleAuthority("FUNC_TAB_HOME"))))
                .andExpect(status().isForbidden());

        verify(listCampaignsUseCase, never()).execute();
    }

    @Test
    void theAdminListIsServedForFuncCampaignRead() throws Exception {
        given(listCampaignsUseCase.execute()).willReturn(List.of());

        mockMvc.perform(get("/api/campaigns/admin")
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_READ"))))
                .andExpect(status().isOk());
    }

    @Test
    void aSingleCampaignReadIsRejectedWithoutFuncCampaignRead() throws Exception {
        mockMvc.perform(get("/api/campaigns/admin/{id}", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_MANAGE"))))
                .andExpect(status().isForbidden());
    }

    // ---- create/edit/screens require FUNC_CAMPAIGN_MANAGE ----

    @Test
    void createIsRejectedForAReadOnlyAdmin() throws Exception {
        mockMvc.perform(post("/api/campaigns/admin")
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_READ")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(campaignRequestJson()))
                .andExpect(status().isForbidden());

        verify(createCampaignUseCase, never()).execute(any());
    }

    @Test
    void createIsAllowedForFuncCampaignManage() throws Exception {
        given(mapper.toResponse(any())).willReturn(new CampaignResponse().id(CAMPAIGN_ID));

        mockMvc.perform(post("/api/campaigns/admin")
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_MANAGE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(campaignRequestJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void deletingACampaignIsRejectedForAPublisherWithoutManage() throws Exception {
        mockMvc.perform(delete("/api/campaigns/admin/{id}", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_PUBLISH"))))
                .andExpect(status().isForbidden());

        verify(deleteCampaignUseCase, never()).execute(any());
    }

    @Test
    void removingAScreenIsRejectedWithoutFuncCampaignManage() throws Exception {
        mockMvc.perform(delete("/api/campaigns/admin/{id}/screens/{screenId}", CAMPAIGN_ID, SCREEN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_READ"))))
                .andExpect(status().isForbidden());

        verify(removeCampaignScreenUseCase, never()).execute(any());
    }

    // ---- publish/pause/archive require FUNC_CAMPAIGN_PUBLISH ----

    @Test
    void publishIsRejectedForAnEditorWhoCannotPublish() throws Exception {
        mockMvc.perform(post("/api/campaigns/admin/{id}/publish", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_MANAGE"))))
                .andExpect(status().isForbidden());

        verify(publishCampaignUseCase, never()).execute(any());
    }

    @Test
    void publishIsAllowedForFuncCampaignPublish() throws Exception {
        given(mapper.toResponse(any())).willReturn(new CampaignResponse().id(CAMPAIGN_ID));

        mockMvc.perform(post("/api/campaigns/admin/{id}/publish", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_PUBLISH"))))
                .andExpect(status().isOk());

        verify(publishCampaignUseCase).execute(any());
    }

    @Test
    void archiveIsRejectedForAnEditorWhoCannotPublish() throws Exception {
        mockMvc.perform(post("/api/campaigns/admin/{id}/archive", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_MANAGE"))))
                .andExpect(status().isForbidden());

        verify(archiveCampaignUseCase, never()).execute(any());
    }

    @Test
    void pauseIsRejectedForAnEditorWhoCannotPublish() throws Exception {
        mockMvc.perform(post("/api/campaigns/admin/{id}/pause", CAMPAIGN_ID)
                        .with(jwt().authorities(new SimpleAuthority("FUNC_CAMPAIGN_MANAGE"))))
                .andExpect(status().isForbidden());

        verify(pauseCampaignUseCase, never()).execute(any());
    }

    private static String campaignRequestJson() {
        return """
                {"name":"Launch week","startAt":"2026-03-01T00:00:00Z","endAt":"2026-04-01T00:00:00Z",
                 "priority":1,"audience":"ALL","frequencyType":"ALWAYS"}
                """;
    }

    /** A bare authority, matching how SecurityConfig reads the "authorities" claim (no ROLE_/SCOPE_ prefix). */
    private record SimpleAuthority(String authority)
            implements org.springframework.security.core.GrantedAuthority {
        @Override
        public String getAuthority() {
            return authority;
        }
    }
}
