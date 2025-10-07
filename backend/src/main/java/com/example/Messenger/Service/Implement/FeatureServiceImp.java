package com.example.Messenger.Service.Implement;


import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.FeatureRequest;
import com.example.Messenger.Repository.FeatureRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Service.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureServiceImp implements FeatureService {
    private final ProductRepository productRepository;

    private final FeatureRepository featureRepository;
    @Autowired
    public FeatureServiceImp(ProductRepository productRepository, FeatureRepository featureRepository) {
        this.productRepository = productRepository;
        this.featureRepository = featureRepository;
    }


    @Override
    public Feature addFeatureToProduct(FeatureRequest request) {
        Product product = productRepository.findById(String.valueOf(request.productId()))
                .orElseThrow(() -> new RuntimeException("❌ Product not found"));

        Feature feature = new Feature();
        feature.setName(request.name());
        feature.setValue(request.value());
        feature.setProduct(product);

        Feature saved = featureRepository.save(feature);

        product.getFeatures().add(saved);
        productRepository.save(product);

        return saved;
    }


    @Override
    public Feature updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Feature not found"));

        feature.setName(request.name());
        feature.setValue(request.value());

        // nếu có thay đổi productId thì chuyển sang product khác
        if (!feature.getProduct().getId().equals(request.productId())) {
            Product newProduct = productRepository.findById(request.productId())
                    .orElseThrow(() -> new RuntimeException("❌ Product not found"));
            feature.setProduct(newProduct);
        }

        return featureRepository.save(feature);
    }


    @Override
    public void deleteFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Feature not found"));

        featureRepository.delete(feature);
    }

}
