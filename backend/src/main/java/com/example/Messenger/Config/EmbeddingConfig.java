package com.example.Messenger.Config;
import com.example.Messenger.Service.EmbeddingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Configuration
public class EmbeddingConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public EmbeddingService embeddingService(RestTemplate restTemplate) {
        return new EmbeddingService(restTemplate);
    }
}