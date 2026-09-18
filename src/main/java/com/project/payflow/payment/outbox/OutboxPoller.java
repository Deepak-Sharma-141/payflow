package com.project.payflow.payment.outbox;

import com.project.payflow.common.config.KafkaProperties;
import com.project.payflow.common.enums.OutboxStatus;
import com.project.payflow.payment.entity.OutboxEvent;
import com.project.payflow.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final Integer MAX_ATTEMPTS = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Scheduled(fixedDelay = 5000)
    public void poll(){

            List<OutboxEvent> pendingEvents = outboxEventRepository
                    .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

            for (OutboxEvent event : pendingEvents) {
                try {
                    String topic = kafkaProperties.topicFor(event.getAggregateType());
                    String key = extractMerchantId(event.getPayload());

                    Map<String, Object> envelope = Map.of(
                            "eventType", event.getEventType(),
                            "aggregateType", event.getAggregateType().name(),
                            "aggregateId", event.getAggregateId().toString(),
                            "data", event.getPayload()
                    );

                    kafkaTemplate.send(topic, key, envelope)
                            .get(5, TimeUnit.SECONDS);

                    handleEventPublished(event);
                } catch (Exception e) {
                    log.error("Outbox event failed eventId: {}, attempts: {}", event.getId(), event.getAttempts());
                    handleEventFailed(event, e.getMessage());
                }
            }
    }

    private void handleEventFailed(OutboxEvent event, String errorMessage) {
        event.setAttempts(event.getAttempts()+1);
        event.setLastError(
                errorMessage.length() < 1000 ? errorMessage : errorMessage.substring(0, 1000));
        if(event.getAttempts() >= MAX_ATTEMPTS){
            event.setStatus(OutboxStatus.FAILED);
        }
        outboxEventRepository.save(event);
    }

    private void handleEventPublished(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }

    private String extractMerchantId(Map<String, Object> payload){
        Object value = payload.get("merchantId");
        return value != null ? value.toString() : "unknown";
    }
}
