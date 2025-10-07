package com.example.Messenger.Controller;

import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Record.FeatureRequest;
import com.example.Messenger.Service.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/features")
public class FeatureController {

    private final FeatureService featureService;
    @Autowired
    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @PostMapping
    public ResponseEntity<Feature> addFeature(@RequestBody FeatureRequest request) {
        Feature savedFeature = featureService.addFeatureToProduct(request);
        return ResponseEntity.ok(savedFeature);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Feature> updateFeature(
            @PathVariable Long id,
            @RequestBody FeatureRequest request
    ) {
        Feature updated = featureService.updateFeature(id, request);
        return ResponseEntity.ok(updated);
    }

    //  Xoá feature
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}