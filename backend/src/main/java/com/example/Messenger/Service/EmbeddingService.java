package com.example.Messenger.Service;

import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate;

    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public float[] generateEmbedding(Product product) {
        String url = "http://localhost:4891/v1/embeddings"; // đúng port như Postman

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(product.getName()).append(" ");
        contentBuilder.append(product.getDescription()).append(" ");

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            contentBuilder.append("Category: ").append(product.getCategory().getName()).append(" ");
        }

        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            for (Feature feature : product.getFeatures()) {
                contentBuilder.append("Feature: ").append(feature.getName()).append(" ");
                if (feature.getValue() != null) {
                    contentBuilder.append(feature.getValue()).append(" ");
                }
            }
        }

        String content = contentBuilder.toString().trim();

        // ✅ Headers: JSON chứ không phải form
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ Payload JSON
        Map<String, Object> payload = Map.of(
                "model", "qwen3-embedding-4b", // model giống Postman của Châu
                "input", content
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            System.out.println("test url " + url);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Không nhận được 'data' từ API!");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            List<Double> embedding = (List<Double>) data.get(0).get("embedding");

            // ✅ Lưu embedding dạng JSON string
            String embeddingJson = new ObjectMapper().writeValueAsString(embedding);
            product.setEmbedding(embeddingJson);

            // ✅ Chuyển List<Double> → float[]
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i).floatValue();
            }

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi embedding API: " + e.getMessage(), e);
        }
    }


}
