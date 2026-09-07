package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.CampaignEventPublisherPort;
import com.skateboard.appconfig.domain.model.CampaignEvent;
import org.springframework.stereotype.Component;

/**
 * V1 sink for {@link CampaignEventPublisherPort}: a plain insert into
 * {@code campaign_event}. A future swap to a real analytics pipeline replaces
 * only this class.
 */
@Component
public class CampaignEventPersistenceAdapter implements CampaignEventPublisherPort {

    private final SpringCampaignEventRepository jpaRepository;

    public CampaignEventPersistenceAdapter(SpringCampaignEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void publish(CampaignEvent event) {
        CampaignEventJpaEntity entity = new CampaignEventJpaEntity();
        entity.setId(event.id());
        entity.setCampaignId(event.campaignId());
        entity.setScreenId(event.screenId());
        entity.setEventType(event.type());
        entity.setOccurredAt(event.occurredAt());
        entity.setPlatform(event.platform());
        entity.setAppVersion(event.appVersion());
        entity.setActionTarget(event.actionTarget());
        jpaRepository.save(entity);
    }
}
