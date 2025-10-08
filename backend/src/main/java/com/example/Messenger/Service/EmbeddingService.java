package com.example.Messenger.Service;

import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.ProductEmbedding;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    private static final String PYTHON_SERVER_URL = "http://localhost:8000/embed";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> generateEmbedding(ProductEmbedding product) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductEmbedding> request = new HttpEntity<>(product, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                PYTHON_SERVER_URL,
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody();
    }
}
