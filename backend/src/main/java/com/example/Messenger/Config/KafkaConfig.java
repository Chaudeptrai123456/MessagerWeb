package com.example.Messenger.Config;

import com.example.Messenger.Record.OrderRequest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
//
//@Configuration
//public class KafkaConfig implements ApplicationListener<ApplicationReadyEvent> {
//    @Autowired
//    private   KafkaAdmin kafkaAdmin;
//    @Autowired
//    private   KafkaTemplate<String, Object> kafkaTemplate;
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//
//    // 🧩 Tự tạo topic nếu chưa tồn tại
//    @Bean
//    public NewTopic analysisTopic() {
//        return TopicBuilder.name("analysis-topic")
//                .partitions(1)
//                .replicas(1)
//                .build();
//    }
//
//    // 🔍 Kiểm tra Kafka kết nối khi app khởi động
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent event) {
//        try {
//            kafkaTemplate.send("analysis-topic", "ping", "health-check").get();
//            System.out.println("✅ Đã kết nối Kafka thành công tại: " + bootstrapServers);
//        } catch (Exception e) {
//            System.err.println("❌ Không thể kết nối tới Kafka tại: " + bootstrapServers);
//            e.printStackTrace();
//        }
//    }
//    @Bean
//    public ProducerFactory<String, OrderRequest> producerFactory() {
//        // Creating a Map
//        Map<String, Object> config = new HashMap<>();
//        // Adding Configuration
//        // 127.0.0.1:9092 is the default port number for
//        // kafka
//        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//                "127.0.0.1:9092");
//        config.put(
//                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        config.put(
//                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//
//        return new DefaultKafkaProducerFactory<>(config);
//    }
//    // Annotation
//    @Bean
//    // Method
//    public KafkaTemplate kafkaTemplate()
//    {
//        return new KafkaTemplate<>(producerFactory());
//    }
//}

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    // 🧩 Tạo topic nếu chưa tồn tại
    @Bean
    public NewTopic analysisTopic() {
        return TopicBuilder.name("analysis-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
    // 🏭 Tạo ProducerFactory cho OrderRequest
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); // optional: tránh lỗi type header
        return new DefaultKafkaProducerFactory<>(config);
    }
    // 🛠 Tạo KafkaTemplate
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    @Bean
    public ConsumerFactory<String, OrderRequest> consumerFactory() {
        JsonDeserializer<OrderRequest> deserializer = new JsonDeserializer<>(OrderRequest.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props,  new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}