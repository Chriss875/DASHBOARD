package org.udsm.udsm_hackathon2026.publisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.udsm.udsm_hackathon2026.dto.WebSocketPayload;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String topic, WebSocketPayload payload) {
        log.debug("Publishing to {}: {}", topic, payload);
        messagingTemplate.convertAndSend(topic, payload);
    }
}