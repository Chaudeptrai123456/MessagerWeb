package com.example.Messenger.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final KafkaAdmin kafkaAdmin;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public KafkaConfig(KafkaAdmin kafkaAdmin, KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaAdmin = kafkaAdmin;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 🧩 Tự tạo topic nếu chưa tồn tại
    @Bean
    public NewTopic analysisTopic() {
        return TopicBuilder.name("analysis-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // 🔍 Kiểm tra Kafka kết nối khi app khởi động
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            kafkaTemplate.send("analysis-topic", "ping", "health-check").get();
            System.out.println("✅ Đã kết nối Kafka thành công tại: " + bootstrapServers);
        } catch (Exception e) {
            System.err.println("❌ Không thể kết nối tới Kafka tại: " + bootstrapServers);
            e.printStackTrace();
        }
    }
}