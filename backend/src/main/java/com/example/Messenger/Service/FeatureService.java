package com.example.Messenger.Service;


import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Record.FeatureRequest;

public interface FeatureService {
    Feature addFeatureToProduct(FeatureRequest request);
    Feature updateFeature(Long id, FeatureRequest request);
    void deleteFeature(Long id);}