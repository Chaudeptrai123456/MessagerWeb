package com.example.Messenger.Service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaHealthCheck {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String HEALTH_CHECK_TOPIC = "health-check-topic";

    public KafkaHealthCheck(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkKafkaConnection() {
        try {
            CompletableFuture<RecordMetadata> future =
                    kafkaTemplate.send(HEALTH_CHECK_TOPIC, "ping", "health-check")
                            .thenApply(result -> result.getRecordMetadata());

            future.thenAccept(metadata -> {
                System.out.printf("✅ Kafka OK: topic='%s', partition=%d, offset=%d%n",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }).exceptionally(ex -> {
                System.err.println("❌ Kafka connection failed: " + ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            System.err.println("❌ Kafka send error:");
            e.printStackTrace();
        }
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        // Nếu message bị deserialize sai → skip qua record lỗi thay vì chết consumer
        return new DefaultErrorHandler(new FixedBackOff(0L, 0));
    }
}
