package com.example.Messenger.Service;


import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductForAnalysis(Object product) {
        sendMessage("analysis-topic", "product", product);
    }

    public void sendOrderForAnalysis(Object order) {
        sendMessage("analysis-topic", "order", order);
    }

    // 🔥 Hàm dùng chung để gửi message + log
    private void sendMessage(String topic, String key, Object value) {
        CompletableFuture<RecordMetadata> future =
                kafkaTemplate.send(topic, key, value)
                        .thenApply(result -> result.getRecordMetadata());
        future.thenAccept(metadata -> {
            System.out.printf(
                    "📤 Gửi thành công [%s] tới topic '%s' (partition=%d, offset=%d)%n",
                    key, topic, metadata.partition(), metadata.offset()
            );
        }).exceptionally(ex -> {
            System.err.printf("❌ Lỗi khi gửi message [%s] tới topic '%s': %s%n",
                    key, topic, ex.getMessage());
            return null;
        });
    }
}