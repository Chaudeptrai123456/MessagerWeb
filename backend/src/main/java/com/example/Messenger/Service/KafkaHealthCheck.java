package com.example.Messenger.Service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaHealthCheck {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaHealthCheck(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkKafkaConnection() {
        try {
            CompletableFuture<RecordMetadata> future =
                    kafkaTemplate.send("analysis-topic", "ping", "health-check")
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
}